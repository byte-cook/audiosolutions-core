package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import de.kobich.component.file.FileDescriptor;

/**
 * Audio file result.
 * @author ckorn
 */
public class AudioFileResult {
	private final Set<FileDescriptor> succeededFiles;
	private final Set<File> createdFiles;
	private final Set<File> deletedFiles;
	private final Set<FileDescriptor> failedFiles;
	
	public AudioFileResult(Set<FileDescriptor> succeededFiles, Set<File> createdFiles, Set<File> deletedFiles, Set<FileDescriptor> failedFiles) {
		this.succeededFiles = Collections.unmodifiableSet(succeededFiles);
		this.createdFiles = Collections.unmodifiableSet(createdFiles);
		this.deletedFiles = Collections.unmodifiableSet(deletedFiles);
		this.failedFiles = Collections.unmodifiableSet(failedFiles);
	}

	public Set<FileDescriptor> getSucceededFiles() {
		return succeededFiles;
	}

	public Set<FileDescriptor> getFailedFiles() {
		return failedFiles;
	}

	public Set<File> getCreatedFiles() {
		return createdFiles;
	}

	public Set<File> getDeletedFiles() {
		return deletedFiles;
	}
}
