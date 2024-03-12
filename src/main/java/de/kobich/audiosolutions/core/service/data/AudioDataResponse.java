package de.kobich.audiosolutions.core.service.data;

import java.util.List;

import de.kobich.component.file.FileDescriptor;

/**
 * Audio data response.
 * @author ckorn
 */
public class AudioDataResponse {
	private List<FileDescriptor> succeededFiles;
	private List<FileDescriptor> failedFiles;
	
	public AudioDataResponse(List<FileDescriptor> succeededFiles, List<FileDescriptor> failedFiles) {
		this.succeededFiles = succeededFiles;
		this.failedFiles = failedFiles;
	}

	/**
	 * @return the succeededFiles
	 */
	public List<FileDescriptor> getSucceededFiles() {
		return succeededFiles;
	}

	/**
	 * @return the failedFiles
	 */
	public List<FileDescriptor> getFailedFiles() {
		return failedFiles;
	}
}
