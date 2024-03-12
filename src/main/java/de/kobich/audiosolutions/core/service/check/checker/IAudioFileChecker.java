package de.kobich.audiosolutions.core.service.check.checker;

import java.util.Set;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.check.CheckAudioFilesOptions;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

public interface IAudioFileChecker {
	/**
	 * Indicates if the given file is supported
	 * @param fileDescriptor
	 * @return
	 */
	boolean supports(FileDescriptor fileDescriptor);
	
	/**
	 * Checks audio files
	 * @param fileDescriptors
	 * @param streams
	 * @throws AudioException
	 */
	void checkAudioFiles(Set<FileDescriptor> fileDescriptors, CheckAudioFilesOptions options, CommandLineStreams streams) throws AudioException;
	
	/**
	 * Returns the command line tool
	 * @return
	 */
	CommandLineTool getCommandLineTool();
}
