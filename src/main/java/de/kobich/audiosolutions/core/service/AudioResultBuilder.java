package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.kobich.component.file.FileDescriptor;

public class AudioResultBuilder {
	public final Set<FileDescriptor> succeededFiles;
	public final Set<File> createdFiles;
	public final Set<File> deletedFiles;
	public final Map<FileDescriptor, FileDescriptor> replacedFiles;
	private final Set<FileDescriptor> failedFiles;
	
	public AudioResultBuilder() {
		this.succeededFiles = new HashSet<FileDescriptor>();
		this.createdFiles = new HashSet<File>();
		this.deletedFiles = new HashSet<File>();
		this.replacedFiles = new HashMap<FileDescriptor, FileDescriptor>();
		this.failedFiles = new HashSet<>();
	}
	
	public void setMissingAsFailed(Collection<FileDescriptor> allFiles) {
		this.failedFiles.addAll(allFiles);
		this.failedFiles.removeAll(this.succeededFiles);
		this.failedFiles.removeAll(this.replacedFiles.keySet());
	}
	
	public AudioFileResult build() {
		return new AudioFileResult(succeededFiles, createdFiles, deletedFiles, replacedFiles, failedFiles);
	}

}
