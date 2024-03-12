package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.kobich.component.file.FileDescriptor;

public class AudioResultSupport {
	public final Set<FileDescriptor> succeededFiles;
	public final Set<File> createdFiles;
	public final Set<File> deletedFiles;
	public final Map<FileDescriptor, FileDescriptor> replacedFiles;
	
	public AudioResultSupport() {
		this.succeededFiles = new HashSet<FileDescriptor>();
		this.createdFiles = new HashSet<File>();
		this.deletedFiles = new HashSet<File>();
		this.replacedFiles = new HashMap<FileDescriptor, FileDescriptor>();
	}
	
	public AudioFileResult createAudioFileResult(Collection<FileDescriptor> allFiles) {
		Set<FileDescriptor> failedFiles = new HashSet<FileDescriptor>(allFiles); 
		failedFiles.removeAll(succeededFiles);
		return new AudioFileResult(succeededFiles, createdFiles, deletedFiles, replacedFiles, failedFiles);
	}

}
