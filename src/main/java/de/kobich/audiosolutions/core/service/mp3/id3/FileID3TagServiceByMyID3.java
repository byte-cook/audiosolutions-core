package de.kobich.audiosolutions.core.service.mp3.id3;

import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.component.file.FileDescriptor;


/**
 * Analyse id3 tags of mp3 files.
 * @author ckorn
 */
@Service(value=IFileID3TagService.MY_ID3)
public class FileID3TagServiceByMyID3 implements IFileID3TagService {
//	private static final String MP3_FILE_FORMAT = "mp3";
//	private static final Logger logger = Logger.getLogger(FileID3TagServiceByMyID3.class);
	
	public FileID3TagServiceByMyID3() {}
	
	@Override
	public ReadID3TagResponse readID3Tags(Set<FileDescriptor> fileDescriptors, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		/*
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		fileDescriptors.addAll(request.getFileDescriptors());
		Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededID3TagFiles = new HashMap<FileDescriptor, Map<MP3ID3TagType,String>>();
		List<FileDescriptor> failedID3TagFiles = new ArrayList<FileDescriptor>();
		
		try {
			Collections.sort(fileDescriptors, new DefaultFileDescriptorComparator());
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
	
				if (!isMp3File) {
					failedID3TagFiles.add(fileDescriptor);
				}
				else {
					Map<MP3ID3TagType, String> id3Tag2Value = new HashMap<MP3ID3TagType, String>();
	
					MusicMetadataSet srcSet = new MyID3().read(file);
					
					if (srcSet != null) {
						logger.debug("ID3 metadata: " + srcSet);
						IMusicMetadata metadata = srcSet.getSimplified();
						
						id3Tag2Value.put(MP3ID3TagType.ALBUM, metadata.getAlbum());
						if (metadata.getYear() != null) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM_YEAR, metadata.getYear().toString());
						}
						id3Tag2Value.put(MP3ID3TagType.ARTIST, metadata.getArtist());
						id3Tag2Value.put(MP3ID3TagType.GENRE, metadata.getGenreName());
						id3Tag2Value.put(MP3ID3TagType.MP3_BITRATE, metadata.getEncoderSettings());
						id3Tag2Value.put(MP3ID3TagType.MP3_CHANNELS, metadata.getEncoderSettings());
						id3Tag2Value.put(MP3ID3TagType.TRACK, metadata.getSongTitle());
						if (metadata.getDurationSeconds() != null) {
							DateFormat parser = new SimpleDateFormat("ss");
							Date time = parser.parse("" + metadata.getDurationSeconds());
							DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
							id3Tag2Value.put(MP3ID3TagType.DURATION_SECONDS, formatter.format(time));
						}
						if (metadata.getTrackNumberNumeric() != null) {
							id3Tag2Value.put(MP3ID3TagType.TRACK_NO, metadata.getTrackNumberNumeric().toString());
						}
					}
					
					succeededID3TagFiles.put(fileDescriptor, id3Tag2Value);
				}
			}
		}
		catch (IOException exc) {
			throw new AudioException(AudioException.MP3_ID3_READ_ERROR, exc);
		}
		catch (ID3ReadException exc) {
			throw new AudioException(AudioException.MP3_ID3_READ_ERROR, exc);
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.MP3_ID3_READ_ERROR, exc);
		}
		ReadID3TagResponse response = new ReadID3TagResponse(succeededID3TagFiles, failedID3TagFiles);
		return response;
		*/
		throw new UnsupportedOperationException();
	}

	@Override
	public AudioFileResult writeID3TagsByAudioData(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, IServiceProgressMonitor monitor) throws AudioException {
		/*
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		fileDescriptors.addAll(request.getFileDescriptors());
		ID3TagVersion id3TagVersion = request.getId3TagVersion();

		AudioResultSupport result = new AudioResultSupport();
		try {
			Collections.sort(fileDescriptors, new DefaultFileDescriptorComparator());
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				File file = fileDescriptor.getFile();
				boolean isMp3File = MP3_FILE_FORMAT.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
				boolean hasAudioData = fileDescriptor.hasMetaData() && fileDescriptor.getMetaData() instanceof AudioData;
				
				if (isMp3File && hasAudioData) {
					TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MP3_FILE_SAVE_OVERWRITE);
					AudioData audioData = (AudioData) fileDescriptor.getMetaData();
					
					String track = audioData.getAttribute(AudioAttribute.TRACK);
					String trackNo = audioData.getAttribute(AudioAttribute.TRACK_NO);
					String album = audioData.getAttribute(AudioAttribute.ALBUM);
					String albumPublication = audioData.getAttribute(AudioAttribute.ALBUM_PUBLICATION);
					String artist = audioData.getAttribute(AudioAttribute.ARTIST);
					String genre = audioData.getAttribute(AudioAttribute.GENRE);
//					Calendar calendar = GregorianCalendar.getInstance();
//					calendar.setTime(albumPublication);
//					int year = calendar.get(Calendar.YEAR);
	
					MusicMetadataSet srcSet = new MyID3().read(file);
					if (srcSet != null) {
						logger.debug("ID3 metadata: " + srcSet);
						/*
						// write for id3v1
//						if (ID3TagVersion.ALL.equals(id3TagVersion) || ID3TagVersion.ID3_V1.equals(id3TagVersion)) {
//							IMusicMetadata musicMetadata = srcSet.id3v1Raw.values;
//							musicMetadata.setAlbum(album);
//							musicMetadata.setArtist(artist);
//							musicMetadata.setGenreID(genreType.getMp3Id());
//							musicMetadata.setSongTitle(track);
//							if (NumberUtils.isNumber(trackNo)) {
//								musicMetadata.setTrackNumberNumeric(Integer.parseInt(trackNo));
//							}
//							if (NumberUtils.isNumber(albumPublication)) {
//								musicMetadata.setYear(Integer.parseInt(albumPublication));
//							}
//							new MyID3().write(file, file, srcSet, musicMetadata);
//						}
						// write for id3v2
						if (ID3TagVersion.ALL.equals(id3TagVersion) || ID3TagVersion.ID3_V2.equals(id3TagVersion)) {
							IMusicMetadata musicMetadata = srcSet.getSimplified();
							if (!AudioData.DEFAULT_VALUE.equals(album)) {
								musicMetadata.setAlbum(album);
							}
							if (!AudioData.DEFAULT_VALUE.equals(artist)) {
								musicMetadata.setArtist(artist);
							}
							if (!AudioData.DEFAULT_VALUE.equals(genre)) {
								musicMetadata.setGenreName(genre);
							}
							if (!AudioData.DEFAULT_VALUE.equals(track)) {
								musicMetadata.setSongTitle(track);
							}
							if (NumberUtils.isNumber(trackNo)) {
								musicMetadata.setTrackNumberNumeric(Integer.parseInt(trackNo));
							}
							if (NumberUtils.isNumber(albumPublication)) {
								musicMetadata.setYear(Integer.parseInt(albumPublication));
							}
							musicMetadata.mergeValuesIfMissing(srcSet.getSimplified());
							musicMetadata.mergeValuesIfMissing(srcSet.id3v1Clean);
							musicMetadata.mergeValuesIfMissing(srcSet.id3v2Clean);

							File dst = new File(file.getParentFile(), file.getName() + ".tmp");
							new MyID3().write(file, dst, srcSet, musicMetadata);
							
							file.delete();
							if (!dst.renameTo(file)) {
								logger.error("Rename failed");
							}
						}
					}
	
					result.succeededFiles.add(fileDescriptor);
				}
			}
		}
		catch (IOException exc) {
			throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
		}
		catch (ID3ReadException exc) {
			throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.MP3_ID3_WRITE_ERROR, exc);
		}
		return result.createAudioFileResult(fileDescriptors);
		*/
		throw new UnsupportedOperationException();
	}

	@Override
	public AudioFileResult writeSingleID3Tag(Set<FileDescriptor> fileDescriptors, final MP3ID3TagType id3Tag, final String value, final ID3TagVersion id3TagVersion, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		throw new UnsupportedOperationException();
	}

	@Override
	public AudioFileResult writeID3TagsByStructure(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, String fileStructurePattern, IServiceProgressMonitor monitor) throws AudioException {
		throw new UnsupportedOperationException();
	}

}
