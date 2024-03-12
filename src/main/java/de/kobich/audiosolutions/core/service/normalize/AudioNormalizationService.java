package de.kobich.audiosolutions.core.service.normalize;

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
import de.kobich.audiosolutions.core.service.normalize.normalizer.IAudioNormalizer;
import de.kobich.commons.Reject;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioNormalizationService {
	@Autowired
	private AudioNormalizers normalizers;

	/**
	 * Returns the tool to use or null, if no suitable normalizer can be found
	 * @param request
	 * @return
	 * @throws AudioException
	 */
	public CommandLineTool mayNormalize(Set<FileDescriptor> fileDescriptors) {
		if (!fileDescriptors.isEmpty()) {
			List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(fileDescriptors);
			Collections.sort(fileList, new AudioFileDescriptorComparator());
			FileDescriptor file = fileList.get(0);
			IAudioNormalizer normalizer = normalizers.findNormalizer(file);
			if (normalizer != null) {
				return normalizer.getCommandLineTool();
			}
		}
		return null;
	}

	/**
	 * Normalize audio files
	 * @param request
	 * @return
	 * @throws AudioException
	 */
	public AudioFileResult normalize(Set<FileDescriptor> fileDescriptors, IAudioNormalizationOptions normalizationOptions, CommandLineTool tool, CommandLineStreams streams) throws AudioException {
		try {
			Reject.ifEmpty(fileDescriptors);
			
			IAudioNormalizer normalizer = normalizers.findNormalizer(tool);
			if (normalizer == null) {
				throw new AudioException(AudioException.NORMALIZER_NOT_FOUND_ERROR);
			}
			
			Set<FileDescriptor> supportedFiles = new HashSet<FileDescriptor>(fileDescriptors.size());
			for (FileDescriptor file : fileDescriptors) {
				if (normalizer.supports(file)) {
					supportedFiles.add(file);
				}
			}
			
			AudioResultSupport result = new AudioResultSupport();
			try {
				if (!streams.hasCommandDefinitionStream()) {
					InputStream definitionStream = tool.getInternalDefinitionStream(normalizer.getClass());
					if (definitionStream == null) {
						throw new AudioException(AudioException.COMMAND_DEFINITION_NOT_FOUND_ERROR);
					}
					streams.setCommandDefinitionStream(definitionStream);
				}
				
				normalizer.execute(supportedFiles, normalizationOptions, streams);
				result.succeededFiles.addAll(supportedFiles);
			}
			finally {
				streams.close();
			}
			return result.createAudioFileResult(fileDescriptors);
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.NORMALIZE_ERROR, exc);
		}
	}

	/**
	 * Copies internal command definition
	 * @param fileDescriptors
	 * @param targetFile
	 * @return
	 * @throws AudioException
	 */
	public File copyInternalCommandDefinition(CommandLineTool tool, File targetFile) throws AudioException {
		return AudioServiceUtils.copyInternalCommandDefinition(tool, IAudioNormalizer.class, targetFile);
	}

}
