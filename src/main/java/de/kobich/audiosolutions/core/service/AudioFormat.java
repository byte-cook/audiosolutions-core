package de.kobich.audiosolutions.core.service;

import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import de.kobich.component.file.FileDescriptor;

public enum AudioFormat {
	AAC(new String[] {"aac"}),
	AC3(new String[] {"ac3"}),
	FLAC(new String[] {"flac"}),
	OGA(new String[] {"oga"}),
	OGG(new String[] {"ogg"}), 
	M4A(new String[] {"m4a"}), 
	MP3(new String[] {"mp3"}),
	WAV(new String[] {"wav"}), 
	WMA(new String[] {"wma"});
	
	private final String[] extensions;
	
	private AudioFormat(String[] extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * @return the extensions
	 */
	public String[] getExtensions() {
		return extensions;
	}
	
	public String getExtension() {
		return extensions[0];
	}
	
	public static Optional<AudioFormat> getAudioFormat(FileDescriptor fileDescriptor) {
		String extension = FilenameUtils.getExtension(fileDescriptor.getFileName());
		return AudioFormat.getAudioFormat(extension);
	}
	
	public static Optional<AudioFormat> getAudioFormat(String extension) {
		for (AudioFormat f : AudioFormat.values()) {
			if (f.getExtension().equalsIgnoreCase(extension)) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
	}

}
