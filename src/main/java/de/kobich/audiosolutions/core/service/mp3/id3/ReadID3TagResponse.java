package de.kobich.audiosolutions.core.service.mp3.id3;

import java.util.List;
import java.util.Map;

import de.kobich.component.file.FileDescriptor;

/**
 * Response of read id3 tags of mp3 files.
 * @author ckorn
 */
public class ReadID3TagResponse {
	private Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededFiles;
	private List<FileDescriptor> failedFiles;
	
	/**
	 * Constructor
	 * @param succeededPatternFiles the files for which the pattern succeeded
	 * @param failedPatternFiles the files for which the pattern failed
	 */
	public ReadID3TagResponse(Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededPatternFiles, List<FileDescriptor> failedPatternFiles) {
		this.succeededFiles = succeededPatternFiles;
		this.failedFiles = failedPatternFiles;
	}

	/**
	 * @return the succeededFiles
	 */
	public Map<FileDescriptor, Map<MP3ID3TagType, String>> getSucceededFiles() {
		return succeededFiles;
	}

	/**
	 * @return the failedFiles
	 */
	public List<FileDescriptor> getFailedFiles() {
		return failedFiles;
	}
}
