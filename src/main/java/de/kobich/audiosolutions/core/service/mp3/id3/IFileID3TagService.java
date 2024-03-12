package de.kobich.audiosolutions.core.service.mp3.id3;

import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.component.file.FileDescriptor;



/**
 * Defines methods to analyse id3 tags of mp3 files.
 * @author ckorn
 */
public interface IFileID3TagService {
	public static final String JAUDIO_TAGGER = "JAudioTagger";
	public static final String JID3 = "JID3";
	public static final String MY_ID3 = "MyID3";
	
	
	/**
	 * Reads id3 tags of mp3 files and extract audio data
	 * @return extracted audio data
	 */
	ReadID3TagResponse readID3Tags(Set<FileDescriptor> fileDescriptors, @Nullable IServiceProgressMonitor monitor) throws AudioException;

	/**
	 * Writes id3 tags to mp3 files
	 */
	AudioFileResult writeID3TagsByAudioData(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, @Nullable IServiceProgressMonitor monitor) throws AudioException;
	
	/**
	 * Writes id3 tags to mp3 files by structure
	 */
	AudioFileResult writeID3TagsByStructure(Set<FileDescriptor> fileDescriptors, ID3TagVersion id3TagVersion, String fileStructurePattern, @Nullable IServiceProgressMonitor monitor) throws AudioException;

	/**
	 * Writes one id3 tag to mp3 files
	 */
	AudioFileResult writeSingleID3Tag(Set<FileDescriptor> fileDescriptors, MP3ID3TagType tag, String value, ID3TagVersion id3TagVersion, @Nullable IServiceProgressMonitor monitor) throws AudioException;
	
	default Optional<InputStream> readArtwork(Set<FileDescriptor> fileDescriptors) throws AudioException {
		return Optional.empty();
	}
}
