package de.kobich.audiosolutions.core.service.describe;

import java.util.List;

import de.kobich.component.file.FileDescriptor;

public class GetAudioDescriptionRequest {
	private final AudioDescriptionType type;
	private final List<FileDescriptor> fileDescriptors;

	/**
	 * @param type
	 * @param fileDescriptor
	 */
	public GetAudioDescriptionRequest(AudioDescriptionType type, List<FileDescriptor> fileDescriptors) {
		this.type = type;
		this.fileDescriptors = fileDescriptors;
	}

	/**
	 * @return the type
	 */
	public AudioDescriptionType getType() {
		return type;
	}

	/**
	 * @return the fileDescriptors
	 */
	public List<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}

}
