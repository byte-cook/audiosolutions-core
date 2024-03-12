package de.kobich.audiosolutions.core.service.convert.codec;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;


public interface IAudioCodec {
	/**
	 * Executes this codec
	 * @param input
	 * @param output
	 * @param outputFormat
	 * @param convertionOptions
	 * @param logOutputStream
	 * @param errorOutputStream
	 * @throws AudioException
	 */
	void execute(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions, CommandLineStreams streams) throws AudioException;
	
	/**
	 * Indicates if these formats are supported
	 * @param inputFormat
	 * @param outputFormat
	 * @return
	 */
	boolean supports(FileDescriptor fileDescriptor, AudioFormat outputFormat);
	
	/**
	 * Returns the command line tool
	 * @return
	 */
	CommandLineTool getCommandLineTool();
}
