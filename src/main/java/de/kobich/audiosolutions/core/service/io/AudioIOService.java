package de.kobich.audiosolutions.core.service.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioResultBuilder;
import de.kobich.commons.misc.extract.Extractor;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.utils.FileUtils;
import de.kobich.commons.utils.RelativePathUtils;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileException;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.io.CopyFileRequest;
import de.kobich.component.file.io.FileCreationType;
import de.kobich.component.file.io.FileIOService;

/**
 * IO service for audio files. 
 * @author ckorn
 */
@Service
public class AudioIOService {
	private static final Logger logger = Logger.getLogger(AudioIOService.class);
	private static final String DEFAULT_VALUE = "_";
	@Autowired
	private FileIOService fileIOService;
	
	/**
	 * Creates a file structure by a given pattern
	 * @param request
	 * @throws AudioException
	 */
	public AudioFileResult createFileStructureByAudioData(CreateFileStructureByAudioDataRequest request) throws AudioException {
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		fileDescriptors.addAll(request.getFileDescriptors());
		String filePattern = request.getFilePattern();
		filePattern = RelativePathUtils.convertBackslashToSlash(filePattern);
		Map<StructureVariable, AudioAttribute> variableMap = request.getVariableMap();
		File rootDirectory = request.getRootDirectory();
		FileCreationType type = request.getType();
		IServiceProgressMonitor monitor = request.getProgressMonitor();
		// monitor begin
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Create File Structure...", fileDescriptors.size());

		if (!rootDirectory.exists()) {
			throw new AudioException(AudioException.FILE_MISSING, rootDirectory);
		}

		try {
			AudioResultBuilder result = new AudioResultBuilder();

			Collections.sort(fileDescriptors, new DefaultFileDescriptorComparator());
			for (FileDescriptor fileDescriptor : fileDescriptors) {
				// monitor sub task
				progressSupport.monitorSubTask("Creating structure for: " + fileDescriptor.getRelativePath(), 1);

				// create path for new file
				GetFilePathByAudioDataRequest filePathRequest = new GetFilePathByAudioDataRequest(fileDescriptor, rootDirectory, filePattern, variableMap);
				File newFile = getFilePathByAudioData(filePathRequest);
				if (newFile != null) {
					if (newFile.exists()) {
						throw new AudioException(AudioException.FILE_ALREADY_EXISTS);
					}
					// copy/move file
					CopyFileRequest copyFileRequest = new CopyFileRequest(fileDescriptor.getFile(), newFile, type);
					FileResult copyResult = fileIOService.copyFile(copyFileRequest);
					result.createdFiles.addAll(copyResult.getCreatedFiles());
					result.deletedFiles.addAll(copyResult.getDeletedFiles());
					result.succeededFiles.add(fileDescriptor);
				}
			}
			result.setMissingAsFailed(fileDescriptors);
			return result.build();
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (FileException exc) {
			throw new AudioException(AudioException.IO_ERROR, exc);
		}
		finally {
			// monitor end
			progressSupport.monitorEndTask("File structure created");
		}
	}

	/**
	 * Returns a file path by a given pattern 
	 * @param request
	 * @return
	 */
	public File getFilePathByAudioData(GetFilePathByAudioDataRequest request) {
		FileDescriptor fileDescriptor = request.getFileDescriptor();
		File rootDirectory = request.getRootDirectory();
		String filePattern = request.getFilePattern();
		filePattern = RelativePathUtils.convertBackslashToSlash(filePattern);
		Map<StructureVariable, AudioAttribute> variableMap = request.getVariableMap();
		
		if (fileDescriptor.hasMetaData(AudioData.class)) {
			AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
			
			Map<StructureVariable, String> variable2ValueMap = new HashMap<StructureVariable, String>();
			for (StructureVariable variable : variableMap.keySet()) {
				AudioAttribute attribute = variableMap.get(variable);

				String value = DEFAULT_VALUE;
				if (audioData.hasAttribute(attribute) && audioData.getAttribute(attribute) != AudioData.DEFAULT_VALUE) {
					value = audioData.getAttribute(attribute);
					if (AudioAttribute.TRACK_NO.equals(attribute)) {
						// use at least 2 characters (add leading zero if required)
						value = StringUtils.leftPad(value, 2, '0');
					}
				}
				variable2ValueMap.put(variable, value);
			}
			String filePath = Extractor.assemble(filePattern, variable2ValueMap);
			File newFile = new File(rootDirectory, filePath);
			try {
				boolean subDir = FileUtils.isSubDirectory(rootDirectory, newFile);
				if (subDir) {
					return newFile;
				}
				else {
					logger.warn("No sub directory -> skip file: " + fileDescriptor.getRelativePath());
				}
			}
			catch (IOException exc) {
				logger.warn("Error while determining canonical path");
			}
		}
		return null;
	}

}
