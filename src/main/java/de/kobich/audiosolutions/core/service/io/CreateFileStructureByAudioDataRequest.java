package de.kobich.audiosolutions.core.service.io;

import java.io.File;
import java.util.Map;
import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.monitor.progress.ProgressMonitorRequest;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.io.FileCreationType;

public class CreateFileStructureByAudioDataRequest extends ProgressMonitorRequest {
	private final Set<FileDescriptor> fileDescriptors;
	private final File rootDirectory;
	private final String filePattern;
	private final Map<StructureVariable, AudioAttribute> variableMap;
	private FileCreationType type;

	/**
	 * @param fileDescriptors
	 * @param rootDirectory
	 * @param filePattern
	 * @param variableMap
	 */
	public CreateFileStructureByAudioDataRequest(Set<FileDescriptor> fileDescriptors, File rootDirectory, String filePattern, Map<StructureVariable, AudioAttribute> variableMap) {
		this.fileDescriptors = fileDescriptors;
		this.rootDirectory = rootDirectory;
		this.filePattern = filePattern;
		this.variableMap = variableMap;
		this.type = FileCreationType.COPY;
	}
	
	/**
	 * @return the fileDescriptors
	 */
	public Set<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}
	/**
	 * @return the rootDirectory
	 */
	public File getRootDirectory() {
		return rootDirectory;
	}
	/**
	 * @return the filePattern
	 */
	public String getFilePattern() {
		return filePattern;
	}

	/**
	 * @return the variableMap
	 */
	public Map<StructureVariable, AudioAttribute> getVariableMap() {
		return variableMap;
	}

	/**
	 * @return the type
	 */
	public FileCreationType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(FileCreationType type) {
		this.type = type;
	}
}
