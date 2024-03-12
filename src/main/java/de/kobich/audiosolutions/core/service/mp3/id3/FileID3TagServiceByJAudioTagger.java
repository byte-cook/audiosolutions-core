package de.kobich.audiosolutions.core.service.mp3.id3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioResultSupport;
import de.kobich.audiosolutions.core.service.AudioServiceUtils;
import de.kobich.commons.misc.extract.ExtractStructureResponse;
import de.kobich.commons.misc.extract.Extractor;
import de.kobich.commons.misc.extract.IText;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileDescriptorTextAdapter;
import de.kobich.component.file.TextComparator;

/**
 * Analyze id3 tags of mp3 files.
 * Note: This class supports ID3 tags in version 1 and version 2.2
 * @author ckorn
 */
@Service(value=IFileID3TagService.JAUDIO_TAGGER)
@Primary
public class FileID3TagServiceByJAudioTagger implements IFileID3TagService {
	private static final String MP3_FILE_FORMAT = "mp3";
	private static final Logger logger = Logger.getLogger(FileID3TagServiceByJAudioTagger.class);

	public FileID3TagServiceByJAudioTagger() {}

	@Override
	public ReadID3TagResponse readID3Tags(Set<FileDescriptor> fileDescriptors, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>();
		fileDescriptorList.addAll(fileDescriptors);
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		// monitor begin
		ProgressData beginData = new ProgressData("Read ID3 tags...", fileDescriptorList.size());
		progressSupport.monitorBeginTask(beginData);

		Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededID3TagFiles = new HashMap<FileDescriptor, Map<MP3ID3TagType, String>>();
		List<FileDescriptor> failedID3TagFiles = new ArrayList<FileDescriptor>();

		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			try {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));

				// monitor sub task
				ProgressData monitorData = new ProgressData("Reading tags of: " + fileDescriptor.getRelativePath(), 1);
				progressSupport.monitorSubTask(monitorData);

				if (!file.exists() || !isMp3File || file.length() == 0) {
					failedID3TagFiles.add(fileDescriptor);
				}
				else {
					Map<MP3ID3TagType, String> id3Tag2Value = new HashMap<MP3ID3TagType, String>();

					MP3File audioFile = (MP3File) AudioFileIO.read(file);
					JAudioTaggerVersionDelegator tag = getTagVersionWrapper(fileDescriptor, audioFile);

					MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();

					if (tag != null) {
						id3Tag2Value.put(MP3ID3TagType.ALBUM, tag.getFirst(FieldKey.ALBUM));
						id3Tag2Value.put(MP3ID3TagType.ALBUM_YEAR, tag.getFirst(FieldKey.YEAR));
						id3Tag2Value.put(MP3ID3TagType.ARTIST, tag.getFirst(FieldKey.ARTIST));
						String genre = tag.getFirst(FieldKey.GENRE);
						// workaround for (17)Rock
						if (genre != null && genre.startsWith("(")) {
							int index = genre.indexOf(")");
							genre = genre.substring(index + 1);
						}
						id3Tag2Value.put(MP3ID3TagType.GENRE, genre);
						id3Tag2Value.put(MP3ID3TagType.TRACK, tag.getFirst(FieldKey.TITLE));
						try {
							// workaround
							id3Tag2Value.put(MP3ID3TagType.TRACK_NO, tag.getFirst(FieldKey.TRACK));
						} catch (NullPointerException exc) {
							logger.warn("WORDAROUND: Track no cannot be read: " + exc.getMessage());
						} catch (UnsupportedOperationException exc) {
							logger.warn("WORDAROUND: Track no cannot be read: " + exc.getMessage());
						}
						id3Tag2Value.put(MP3ID3TagType.COMMENT, tag.getFirst(FieldKey.COMMENT));
					}
					if (audioHeader != null) {
						id3Tag2Value.put(MP3ID3TagType.MP3_BITRATE, audioHeader.getBitRate() + " kBit/s");
						id3Tag2Value.put(MP3ID3TagType.MP3_CHANNELS, audioHeader.getChannels());
						id3Tag2Value.put(MP3ID3TagType.ENCODING_TYPE, audioHeader.getEncodingType());
						id3Tag2Value.put(MP3ID3TagType.FORMAT, audioHeader.getFormat());
						id3Tag2Value.put(MP3ID3TagType.SAMPLE_RATE, audioHeader.getSampleRate() + " Hz");

						// duration
						DateFormat parser = new SimpleDateFormat("ss");
						Date time = parser.parse("" + audioHeader.getTrackLength());
						DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
						id3Tag2Value.put(MP3ID3TagType.DURATION_SECONDS, formatter.format(time));
					}
					
					succeededID3TagFiles.put(fileDescriptor, id3Tag2Value);
				}
			}
			catch (ProgressCancelException exc) {
				throw exc;
			}
			catch (Exception exc) {
				failedID3TagFiles.add(fileDescriptor);
				logger.error(exc.getMessage(), exc);
//				throw new AudioException(AudioException.MP3_ID3_READ_ERROR, exc);
			}
		}
		// monitor end
		progressSupport.monitorEndTask(new ProgressData("ID3 tags read"));

		ReadID3TagResponse response = new ReadID3TagResponse(succeededID3TagFiles, failedID3TagFiles);
		return response;
	}

	@Override
	public AudioFileResult writeID3TagsByAudioData(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, IServiceProgressMonitor monitor) throws AudioException {
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>();
		fileDescriptorList.addAll(fileDescriptors);
//		ID3TagVersion id3TagVersion = request.getId3TagVersion();
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		// monitor begin
		ProgressData beginData = new ProgressData("Write ID3 tags...", fileDescriptorList.size());
		progressSupport.monitorBeginTask(beginData);

		AudioResultSupport result = new AudioResultSupport();
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			try {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
				boolean hasAudioData = fileDescriptor.hasMetaData(AudioData.class);
				
				// monitor sub task
				ProgressData monitorData = new ProgressData("Writing tags to: " + fileDescriptor.getRelativePath(), 1);
				progressSupport.monitorSubTask(monitorData);

				if (isMp3File && hasAudioData && file.length() > 0) {
					AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
					Map<AudioAttribute, String> attributes = new HashMap<AudioAttribute, String>();
					for (AudioAttribute attribute : AudioAttribute.values()) {
						if (audioData.hasAttribute(attribute)) {
							attributes.put(attribute, audioData.getAttribute(attribute));
						}
					}
					
					ensureID3v2Tags(fileDescriptor);

					writeAudioData(fileDescriptor, attributes);
					result.succeededFiles.add(fileDescriptor);
				}
			}
			catch (Exception exc) {
				logger.error(exc.getMessage(), exc);
//				throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
			}
		}
		// monitor end
		progressSupport.monitorEndTask(new ProgressData("ID3 tags written"));
		return result.createAudioFileResult(fileDescriptorList);
	}

	@Override
	public AudioFileResult writeID3TagsByStructure(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, String fileStructurePattern, IServiceProgressMonitor monitor) throws AudioException {
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		// monitor begin
		ProgressData beginData = new ProgressData("Write ID3 tags...");
		progressSupport.monitorBeginTask(beginData);

		AudioResultSupport result = new AudioResultSupport();
		
		Set<IText> texts = AudioServiceUtils.convert2Texts(fileDescriptors);
		Collection<StructureVariable> variables = AudioAttribute2StructureVariableMapper.getInstance().getVariables();
		ExtractStructureResponse structureResponse = Extractor.extract(texts, fileStructurePattern, variables, monitor);
		Map<IText, Map<StructureVariable, String>> succeededFiles = structureResponse.getSucceededTexts();
		List<IText> failedTexts = structureResponse.getFailedTexts();
		
		// failed files
		Collections.sort(failedTexts, new TextComparator());
		for (IText text : failedTexts) {
			assert text instanceof FileDescriptorTextAdapter;
			FileDescriptor fileDescriptor = ((FileDescriptorTextAdapter) text).getFileDescriptor();
			// monitor sub task
			ProgressData monitorData = new ProgressData("Skipping: " + fileDescriptor.getRelativePath(), 1);
			progressSupport.monitorSubTask(monitorData);
		}
		
		// succeeded files
		AudioAttribute2StructureVariableMapper mapper = AudioAttribute2StructureVariableMapper.getInstance();
		List<IText> succeededTexts = new ArrayList<IText>(succeededFiles.keySet());
		Collections.sort(succeededTexts, new TextComparator());
		for (IText text : succeededTexts) {
			assert text instanceof FileDescriptorTextAdapter;
			FileDescriptor fileDescriptor = ((FileDescriptorTextAdapter) text).getFileDescriptor();
			File file = fileDescriptor.getFile();
			try {
				// monitor sub task
				ProgressData monitorData = new ProgressData("Writing tags to: " + fileDescriptor.getRelativePath(), 1);
				progressSupport.monitorSubTask(monitorData);
				
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
				if (isMp3File && file.length() > 0) {
					Map<AudioAttribute, String> attributes = new HashMap<AudioAttribute, String>();
					Map<StructureVariable, String> variableValues = succeededFiles.get(text);
					for (StructureVariable variable : variableValues.keySet()) {
						AudioAttribute attribute = mapper.getAudioAttribute(variable);
						String value = variableValues.get(variable);
						if (attribute != null) {
							attributes.put(attribute, value);
						}
					}
					
					ensureID3v2Tags(fileDescriptor);
					writeAudioData(fileDescriptor, attributes);
					result.succeededFiles.add(fileDescriptor);
				}
			}
			catch (Exception exc) {
				logger.error(exc.getMessage(), exc);
//				throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
			}
		}
		
		// monitor end
		progressSupport.monitorEndTask(new ProgressData("ID3 tags written"));
		return result.createAudioFileResult(fileDescriptors);
	}

	@Override
	public AudioFileResult writeSingleID3Tag(Set<FileDescriptor> fileDescriptors, final MP3ID3TagType id3Tag, final String value, final ID3TagVersion id3TagVersion, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>();
		fileDescriptorList.addAll(fileDescriptors);
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		// monitor begin
		ProgressData beginData = new ProgressData("Write ID3 tags...", fileDescriptorList.size());
		progressSupport.monitorBeginTask(beginData);
		
		AudioResultSupport result = new AudioResultSupport();
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			try {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
				
				// monitor sub task
				ProgressData monitorData = new ProgressData("Writing tags to: " + fileDescriptor.getRelativePath(), 1);
				progressSupport.monitorSubTask(monitorData);

				if (isMp3File && file.length() > 0) {
					String value2Set = value;
					FieldKey fieldKey = getFieldKey(id3Tag);
					if (fieldKey == null) {
						continue;
					}
					else if (FieldKey.TRACK.equals(fieldKey)) {
						int no = NumberUtils.toInt(value2Set, -1);
						if (no != -1) {
							value2Set = String.valueOf(no);
						}
						else {
							continue;
						}
					}
					ensureID3v2Tags(fileDescriptor);

					MP3File audioFile = (MP3File) AudioFileIO.read(fileDescriptor.getFile());
					JAudioTaggerVersionDelegator tag = getTagVersionWrapper(fileDescriptor, audioFile);
					if (StringUtils.isNotBlank(value2Set)) {
						tag.setField(fieldKey, value2Set);
					}
					else {
						tag.deleteField(fieldKey);
					}
					audioFile.commit();
					
					result.succeededFiles.add(fileDescriptor);
				}
			}
			catch (Exception exc) {
				logger.error(exc.getMessage(), exc);
//				throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
			}
		}
		// monitor end
		progressSupport.monitorEndTask(new ProgressData("ID3 tags written"));
		
		return result.createAudioFileResult(fileDescriptorList);
	}
	
	@Override
	public Optional<InputStream> readArtwork(Set<FileDescriptor> fileDescriptors) throws AudioException {
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			try {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
				if (isMp3File && file.exists() && file.length() > 0) {
					MP3File audioFile = (MP3File) AudioFileIO.read(file);
					JAudioTaggerVersionDelegator tag = getTagVersionWrapper(fileDescriptor, audioFile);
					
					Optional<Artwork> artwork = tag.getArtwork();
					if (artwork.isPresent()) {
						byte[] data = artwork.get().getBinaryData();
						if (data != null) {
							logger.info("Artwork found for: " + file.getAbsolutePath());
							return Optional.of(new ByteArrayInputStream(data));
						}
					}
				}
				
			}
			catch (Exception exc) {
				logger.warn(exc.getMessage(), exc);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Returns the tag wrapper for an audio file
	 * @param fileDescriptor
	 * @param audioFile
	 * @return
	 * @throws CannotReadException
	 * @throws IOException
	 * @throws TagException
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 * @throws CannotWriteException
	 */
	private JAudioTaggerVersionDelegator getTagVersionWrapper(FileDescriptor fileDescriptor, MP3File audioFile) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
		ID3v1Tag id3v1Tag = audioFile.getID3v1Tag();
		if (id3v1Tag == null) {
			// maybe: v22 should be used for Windows Explorer
			id3v1Tag = new ID3v1Tag();
			audioFile.setID3v1Tag((ID3v1Tag) id3v1Tag);
		}
		AbstractID3v2Tag id3v2Tag = audioFile.getID3v2Tag();
		if (id3v2Tag == null) {
			// maybe: v22 should be used for Windows Explorer
			id3v2Tag = new ID3v22Tag();
			audioFile.setID3v2Tag((ID3v22Tag) id3v2Tag);
		}
//		ID3v24Tag id3v24Tag = audioFile.getID3v2TagAsv24();
//		if (id3v24Tag == null) {
//			id3v24Tag = new ID3v24Tag();
//			audioFile.setID3v2Tag((ID3v24Tag) id3v24Tag);
//		}
		JAudioTaggerVersionDelegator tag = new JAudioTaggerVersionDelegator(id3v1Tag, id3v2Tag, null);
		return tag;
	}
	
	/**
	 * Ensures the use of id3 v2 tags
	 * @param fileDescriptor
	 * @throws CannotWriteException
	 * @throws CannotReadException
	 * @throws IOException
	 * @throws TagException
	 * @throws ReadOnlyFileException
	 * @throws InvalidAudioFrameException
	 */
	private void ensureID3v2Tags(FileDescriptor fileDescriptor) throws CannotWriteException, CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		File file = fileDescriptor.getFile();
		MP3File audioFile = (MP3File) AudioFileIO.read(file);
		if (audioFile.hasID3v2Tag()) {
			// is already v2 tags
			return;
		}
		
		ID3v1Tag id3v1Tag = audioFile.getID3v1Tag();
		if (id3v1Tag == null) {
			// has no id3 tags
			return;
		}
		
		// maybe: v22 should be used for Windows Explorer
//		ID3v24Tag id3v24Tag = new ID3v24Tag();
		ID3v22Tag id3v22Tag = new ID3v22Tag();
		for (MP3ID3TagType id3Tag : MP3ID3TagType.values()) {
			try {
				FieldKey fieldKey = getFieldKey(id3Tag);
				if (fieldKey != null) {
					String value = id3v1Tag.getFirst(fieldKey);
					if (StringUtils.isNotBlank(value)) {
//						id3v24Tag.setField(fieldKey, value);
						id3v22Tag.setField(fieldKey, value);
					}
				}
			} catch (UnsupportedOperationException exc) {
				logger.warn("WORDAROUND: Track no cannot be read: " + exc.getMessage());
			}
		}
		audioFile.setID3v2Tag(id3v22Tag);
		
		audioFile.commit();
	}

	/**
	 * Writes id3 tags to a file
	 * @param file
	 * @param attributes
	 */
	private void writeAudioData(FileDescriptor fileDescriptor, Map<AudioAttribute, String> attributes) throws CannotWriteException, CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		String track = attributes.get(AudioAttribute.TRACK);
		String trackNo = attributes.get(AudioAttribute.TRACK_NO);
		if (attributes.containsKey(AudioAttribute.TRACK_NO)) {
			int no = NumberUtils.toInt(trackNo, -1);
			if (no != -1) {
				trackNo = String.valueOf(no);
			}
			else {
				trackNo = AudioData.DEFAULT_VALUE;
			}
		}
		String album = attributes.get(AudioAttribute.ALBUM);
		String albumPublication = attributes.get(AudioAttribute.ALBUM_PUBLICATION);
		String artist = attributes.get(AudioAttribute.ARTIST);
		String genre = attributes.get(AudioAttribute.GENRE);

		MP3File audioFile = (MP3File) AudioFileIO.read(fileDescriptor.getFile());
		JAudioTaggerVersionDelegator tag = getTagVersionWrapper(fileDescriptor, audioFile);
		
		if (track != null && !AudioData.DEFAULT_VALUE.equals(track)) {
			tag.setField(FieldKey.TITLE, track);
		}
		if (trackNo != null && !AudioData.DEFAULT_VALUE.equals(trackNo)) {
			tag.setField(FieldKey.TRACK, trackNo);
		}
		if (album != null && !AudioData.DEFAULT_VALUE.equals(album)) {
			tag.setField(FieldKey.ALBUM, album);
		}
		if (albumPublication != null && !AudioData.DEFAULT_VALUE.equals(albumPublication)) {
			tag.setField(FieldKey.YEAR, albumPublication);
		}
		if (artist != null && !AudioData.DEFAULT_VALUE.equals(artist)) {
			tag.setField(FieldKey.ARTIST, artist);
		}
		if (genre != null && !AudioData.DEFAULT_VALUE.equals(genre)) {
			tag.setField(FieldKey.GENRE, genre);
		}
		audioFile.commit();
	}
	
	/**
	 * Returns the field key
	 * @param id3Tag
	 * @return
	 */
	private FieldKey getFieldKey(MP3ID3TagType id3Tag) {
		switch (id3Tag) {
			case ALBUM:
				return FieldKey.ALBUM;
			case ALBUM_YEAR:
				return FieldKey.YEAR;
			case ARTIST:
				return FieldKey.ARTIST;
			case COMMENT:
				return FieldKey.COMMENT;
			case DURATION_SECONDS:
				break;
			case ENCODING_TYPE:
				break;
			case FORMAT:
				break;
			case GENRE:
				return FieldKey.GENRE;
			case MP3_BITRATE:
				break;
			case MP3_CHANNELS:
				break;
			case SAMPLE_RATE:
				break;
			case TRACK:
				return FieldKey.TITLE;
			case TRACK_NO:
				return FieldKey.TRACK;
				
		}
		return null;
	}
}
