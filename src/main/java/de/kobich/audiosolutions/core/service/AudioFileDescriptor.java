package de.kobich.audiosolutions.core.service;

import java.util.Map;

import de.kobich.component.file.FileDescriptor;

/**
 * Contains a file descriptor with its audio attributes.
 */
public class AudioFileDescriptor {
	private final FileDescriptor fileDescriptor;
	private final Map<AudioAttribute, String> audioDataValues;

	/**
	 * @param fileDescriptor
	 */
	public AudioFileDescriptor(FileDescriptor fileDescriptor, Map<AudioAttribute, String> audioData2Value) {
		this.fileDescriptor = fileDescriptor;
		this.audioDataValues = audioData2Value;
	}
	
	/**
	 * @return the fileDescriptor
	 */
	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	public boolean hasAttribute(AudioAttribute attribute) {
		return audioDataValues.containsKey(attribute);
	}
	
	public String getAttribute(AudioAttribute attribute) {
		return audioDataValues.get(attribute);
	}

	public Map<AudioAttribute, String> getAudioDataValues() {
		return audioDataValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileDescriptor == null) ? 0 : fileDescriptor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AudioFileDescriptor other = (AudioFileDescriptor) obj;
		if (fileDescriptor == null) {
			if (other.fileDescriptor != null)
				return false;
		}
		else if (!fileDescriptor.equals(other.fileDescriptor))
			return false;
		return true;
	}

}
