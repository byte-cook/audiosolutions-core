package de.kobich.audiosolutions.core.service.normalize.normalizer;

import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

public interface IAudioNormalizer {
	/**
	 * Executes this normalizer
	 * @param input
	 * @param normalizationOptions
	 * @param logOutputStream
	 * @param errorOutputStream
	 * @throws AudioException
	 */
	void execute(Set<FileDescriptor> input, IAudioNormalizationOptions normalizationOptions, CommandLineStreams streams) throws AudioException;
	
	/**
	 * Indicates if the given format is supported
	 * @param format
	 * @return
	 */
	boolean supports(FileDescriptor fileDescriptor);
	
	/**
	 * Returns the command line tool
	 * @return
	 */
	CommandLineTool getCommandLineTool();

}
