package de.kobich.audiosolutions.core.service.convert;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioFormatUtil;
import de.kobich.audiosolutions.core.service.AudioResultSupport;
import de.kobich.audiosolutions.core.service.AudioServiceUtils;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.convert.codec.IAudioCodec;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioConversionService {
	@Autowired
	private AudioCodecs audioCodecs;
	
	/**
	 * Returns the tool to use or null, if no suitable codec can be found
	 * @param request
	 * @return
	 * @throws AudioException
	 */
	public CommandLineTool mayConvert(Set<FileDescriptor> fileDescriptors, AudioFormat outputFormat) {
		if (!fileDescriptors.isEmpty()) {
			List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(fileDescriptors);
			Collections.sort(fileList, new AudioFileDescriptorComparator());
			FileDescriptor file = fileList.get(0);
			IAudioCodec codec = audioCodecs.findCodec(file, outputFormat);
			if (codec != null) {
				return codec.getCommandLineTool();
			}
		}
		return null;
	}
	
	/**
	 * Converts audio files
	 * @param request
	 * @throws AudioException
	 */
	public AudioFileResult convert(Set<FileDescriptor> fileDescriptors, File targetDirectory, AudioFormat outputFormat, IAudioConversionOptions convertionOptions, CommandLineTool tool, CommandLineStreams streams) throws AudioException {
		try {
			IAudioCodec codec = audioCodecs.findCodec(tool);
			if (codec == null) {
				throw new AudioException(AudioException.ENCODER_NOT_FOUND_ERROR);
			}
			
			// supported files
			List<FileDescriptor> supportedFiles = new ArrayList<FileDescriptor>();
			for (FileDescriptor file : fileDescriptors) {
				if (codec.supports(file, outputFormat)) {
					supportedFiles.add(file);
				}
			}

			// streams
			if (!streams.hasCommandDefinitionStream()) {
				InputStream definitionStream = tool.getInternalDefinitionStream(IAudioCodec.class);
				if (definitionStream == null) {
					throw new AudioException(AudioException.COMMAND_DEFINITION_NOT_FOUND_ERROR);
				}
				streams.setCommandDefinitionStream(definitionStream);
			}

			// convert each file
			AudioResultSupport result = new AudioResultSupport();
			
			Collections.sort(supportedFiles, new AudioFileDescriptorComparator());
			for (FileDescriptor fileDescriptor : supportedFiles) {
				CommandLineStreams nestedStreams = streams.createNestedStreams();
				try {
					// decode to output format
					FileDescriptor output = AudioFormatUtil.getOutputFile(fileDescriptor, targetDirectory, outputFormat);
					if (!output.getFile().exists()) {
						if (nestedStreams.hasCommandDefinitionStream()) {
							codec.execute(fileDescriptor, output, outputFormat, convertionOptions, nestedStreams);
							result.succeededFiles.add(fileDescriptor);
							result.createdFiles.add(output.getFile());
						}
					}
				}
				finally {
					nestedStreams.close();
				}
			}
			return result.createAudioFileResult(fileDescriptors);
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (Exception exc) {
			throw new AudioException(AudioException.CONVERT_ERROR, exc);
		}
		finally {
			streams.close();
		}
	}

	/**
	 * Copies internal command definition
	 * @param fileDescriptors
	 * @param outputFormat
	 * @param targetFile
	 * @return
	 * @throws AudioException
	 */
	public File copyInternalCommandDefinition(CommandLineTool tool, File targetDirectory) throws AudioException {
		return AudioServiceUtils.copyInternalCommandDefinition(tool, IAudioCodec.class, targetDirectory);
	}

}
