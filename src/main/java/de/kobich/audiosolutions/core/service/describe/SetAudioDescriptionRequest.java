package de.kobich.audiosolutions.core.service.describe;

import java.util.List;

import de.kobich.component.file.FileDescriptor;

public class SetAudioDescriptionRequest {
	private final AudioDescriptionType type;
	private final List<FileDescriptor> fileDescriptors;
	private final String description;

	/**
	 * @param type
	 * @param fileDescriptor
	 */
	public SetAudioDescriptionRequest(AudioDescriptionType type, List<FileDescriptor> fileDescriptors, String description) {
		this.type = type;
		this.fileDescriptors = fileDescriptors;
		this.description = description;
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

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
