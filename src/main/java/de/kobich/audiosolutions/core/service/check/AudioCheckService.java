package de.kobich.audiosolutions.core.service.check;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioResultSupport;
import de.kobich.audiosolutions.core.service.AudioServiceUtils;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.check.checker.IAudioFileChecker;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioCheckService {
	@Autowired
	private List<IAudioFileChecker> checkers;

	/**
	 * Returns the tool to use or null, if no suitable tool can be found
	 * @param fileDescriptors
	 * @return
	 */
	public CommandLineTool mayCheckAudioFiles(Set<FileDescriptor> fileDescriptors) {
		if (!fileDescriptors.isEmpty()) {
			List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(fileDescriptors);
			Collections.sort(fileList, new AudioFileDescriptorComparator());
			FileDescriptor file = fileList.get(0);
			for (IAudioFileChecker checker : checkers) {
				if (checker.supports(file)) {
					return checker.getCommandLineTool();
				}
			}
		}
		return null;
	}

	/**
	 * Checks audio files
	 * @param request
	 * @return
	 * @throws AudioException
	 */
	public AudioFileResult checkAudioFiles(final Set<FileDescriptor> fileDescriptors, CheckAudioFilesOptions options, CommandLineTool tool, final CommandLineStreams streams) throws AudioException {
		IAudioFileChecker checker = findChecker(tool);
		
		// collect files for each checker
		Set<FileDescriptor> supportedFiles = new HashSet<FileDescriptor>(fileDescriptors.size());
		for (FileDescriptor file : fileDescriptors) {
			if (checker.supports(file)) {
				supportedFiles.add(file);
			}
		}
		
		// check files
		AudioResultSupport result = new AudioResultSupport();
		try {
			if (!streams.hasCommandDefinitionStream()) {
				InputStream definitionStream = tool.getInternalDefinitionStream(checker.getClass());
				if (definitionStream == null) {
					throw new AudioException(AudioException.COMMAND_DEFINITION_NOT_FOUND_ERROR);
				}
				streams.setCommandDefinitionStream(definitionStream);
			}
			
			checker.checkAudioFiles(supportedFiles, options, streams);
			result.succeededFiles.addAll(supportedFiles);
		}
		finally {
			streams.close();
		}
		return result.createAudioFileResult(fileDescriptors);
	}
	
	private IAudioFileChecker findChecker(CommandLineTool tool) throws AudioException {
		for (IAudioFileChecker checker : checkers) {
			if (tool.equals(checker.getCommandLineTool())) {
				return checker;
			}
		}
		throw new AudioException(AudioException.CHECKER_NOT_FOUND_ERROR);
	}

	/**
	 * Copies internal command definition
	 * @param targetFile
	 * @return
	 * @throws AudioException
	 */
	public File copyInternalCommandDefinition(CommandLineTool tool, File targetFile) throws AudioException {
		return AudioServiceUtils.copyInternalCommandDefinition(tool, IAudioFileChecker.class, targetFile);
	}
}
