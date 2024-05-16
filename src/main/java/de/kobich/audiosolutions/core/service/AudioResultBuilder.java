package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.kobich.component.file.FileDescriptor;

public class AudioResultBuilder {
	public final Set<FileDescriptor> succeededFiles;
	public final Set<File> createdFiles;
	public final Set<File> deletedFiles;
	private final Set<FileDescriptor> failedFiles;
	
	public AudioResultBuilder() {
		this.succeededFiles = new HashSet<FileDescriptor>();
		this.createdFiles = new HashSet<File>();
		this.deletedFiles = new HashSet<File>();
		this.failedFiles = new HashSet<>();
	}
	
	public void setMissingAsFailed(Collection<FileDescriptor> allFiles) {
		this.failedFiles.addAll(allFiles);
		this.failedFiles.removeAll(this.succeededFiles);
	}
	
	public AudioFileResult build() {
		return new AudioFileResult(succeededFiles, createdFiles, deletedFiles, failedFiles);
	}

}
