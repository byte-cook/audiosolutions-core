package de.kobich.audiosolutions.core.service.io;

import java.io.File;
import java.util.Map;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.component.file.FileDescriptor;

public class GetFilePathByAudioDataRequest {
	private final FileDescriptor fileDescriptor;
	private final File rootDirectory;
	private final String filePattern;
	private final Map<StructureVariable, AudioAttribute> variableMap;
	
	/**
	 * @param fileDescriptor
	 * @param rootDirectory
	 * @param filePattern
	 * @param variableMap
	 */
	public GetFilePathByAudioDataRequest(FileDescriptor fileDescriptor, File rootDirectory, String filePattern, Map<StructureVariable, AudioAttribute> variableMap) {
		this.fileDescriptor = fileDescriptor;
		this.rootDirectory = rootDirectory;
		this.filePattern = filePattern;
		this.variableMap = variableMap;
	}
	
	/**
	 * @return the fileDescriptor
	 */
	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
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
}
