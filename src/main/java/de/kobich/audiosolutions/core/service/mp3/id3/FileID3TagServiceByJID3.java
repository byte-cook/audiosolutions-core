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
@Service(value=IFileID3TagService.JID3)
public class FileID3TagServiceByJID3 implements IFileID3TagService {
//	private static final String MP3_FILE_FORMAT = "mp3";
	
	public FileID3TagServiceByJID3() {}

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
	
					MP3File mp3File = new MP3File(file);
	  				TagOptionSingleton.getInstance().setDefaultSaveMode(TagConstant.MASK_V23_READ_ONLY);
	
	
					// TODO: mp3 methods still not supported from api
					//System.out.println(mp3File.getMpegVersion());
					//System.out.println(mp3File.getLayer());
					//System.out.println(mp3File.getBitRate());
					//System.out.println(mp3File.getMode());
					//System.out.println(mp3File.getFrequency());
					
					if (mp3File.hasID3v1Tag()) {
						ID3v1 id3v1 = mp3File.getID3v1Tag();
						if (StringUtils.hasText(id3v1.getAlbum())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM, id3v1.getAlbum());
						}
						if (StringUtils.hasText(id3v1.getAlbumTitle())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM, id3v1.getAlbumTitle());
						}
						if (StringUtils.hasText(id3v1.getArtist())) {
							id3Tag2Value.put(MP3ID3TagType.ARTIST, id3v1.getArtist());
						}
						if (StringUtils.hasText(id3v1.getLeadArtist())) {
							id3Tag2Value.put(MP3ID3TagType.ARTIST, id3v1.getLeadArtist());
						}
						if (StringUtils.hasText(id3v1.getSongGenre())) {
							MP3ID3GenreType genre = MP3ID3GenreType.getByMp3Id(Integer.parseInt(id3v1.getSongGenre()));
							id3Tag2Value.put(MP3ID3TagType.GENRE, genre.name());
						}
						if (StringUtils.hasText(id3v1.getTitle())) {
							id3Tag2Value.put(MP3ID3TagType.TRACK, id3v1.getTitle());
						}
						if (StringUtils.hasText(id3v1.getSongTitle())) {
							id3Tag2Value.put(MP3ID3TagType.TRACK, id3v1.getSongTitle());
						}
						if (StringUtils.hasText(id3v1.getYear())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM_YEAR, id3v1.getYear());
						}
						if (StringUtils.hasText(id3v1.getYearReleased())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM_YEAR, id3v1.getYearReleased());
						}
					}
					if (mp3File.hasID3v2Tag()) {
						AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
						if (StringUtils.hasText(id3v2.getAlbumTitle())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM, id3v2.getAlbumTitle());
						}
						if (StringUtils.hasText(id3v2.getLeadArtist())) {
							id3Tag2Value.put(MP3ID3TagType.ARTIST, id3v2.getLeadArtist());
						}
						if (StringUtils.hasText(id3v2.getSongGenre())) {
							MP3ID3GenreType genre = MP3ID3GenreType.getByMp3Id(Integer.parseInt(id3v2.getSongGenre()));
							id3Tag2Value.put(MP3ID3TagType.GENRE, genre.name());
						}
						if (StringUtils.hasText(id3v2.getSongTitle())) {
							id3Tag2Value.put(MP3ID3TagType.TRACK, id3v2.getSongTitle());
						}
						if (StringUtils.hasText(id3v2.getTrackNumberOnAlbum())) {
							id3Tag2Value.put(MP3ID3TagType.TRACK_NO, id3v2.getTrackNumberOnAlbum());
						}
						if (StringUtils.hasText(id3v2.getYearReleased())) {
							id3Tag2Value.put(MP3ID3TagType.ALBUM_YEAR, id3v2.getYearReleased());
						}
					}
					succeededID3TagFiles.put(fileDescriptor, id3Tag2Value);
				}
			}
		}
		catch (IOException exc) {
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
					MP3ID3GenreType genreType = MP3ID3GenreType.UNKNOWN;
					if (genre != null) {
						genreType = MP3ID3GenreType.valueOf(genre);
					}
	
					MP3File mp3File = new MP3File(file);
					// write for id3v1
					if (ID3TagVersion.ALL.equals(id3TagVersion) || ID3TagVersion.ID3_V1.equals(id3TagVersion)) {
						ID3v1_1 id3v1 = new ID3v1_1();
						id3v1.setAlbumTitle(album);
						id3v1.setLeadArtist(artist);
						id3v1.setSongGenre("" + genreType.getMp3Id());
						id3v1.setSongTitle(track);
						if (NumberUtils.isNumber(trackNo)) {
							id3v1.setTrackNumberOnAlbum(trackNo);
						}
						id3v1.setYearReleased(albumPublication);
						mp3File.setID3v1Tag(id3v1);
					}
	
					// write for id3v2
					if (ID3TagVersion.ALL.equals(id3TagVersion) || ID3TagVersion.ID3_V2.equals(id3TagVersion)) {
						AbstractID3v2 id3v2 = new ID3v2_3();
						id3v2.setAlbumTitle(album);
						id3v2.setLeadArtist(artist);
						id3v2.setSongGenre("" + genreType.getMp3Id());
						id3v2.setSongTitle(track);
						if (NumberUtils.isNumber(trackNo)) {
							id3v2.setTrackNumberOnAlbum(trackNo);
						}
						id3v2.setYearReleased(albumPublication);
						mp3File.setID3v2Tag(id3v2);
					}
	
					mp3File.save(TagConstant.MP3_FILE_SAVE_OVERWRITE);
					result.succeededFiles.add(fileDescriptor);
				}
			}
		}
		catch (IOException exc) {
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
