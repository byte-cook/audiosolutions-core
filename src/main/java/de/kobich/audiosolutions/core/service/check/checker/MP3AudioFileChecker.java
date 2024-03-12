package de.kobich.audiosolutions.core.service.check.checker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.check.CheckAudioFilesOptions;
import de.kobich.commons.runtime.executor.ExecuteRequest;
import de.kobich.commons.runtime.executor.Executor;
import de.kobich.commons.runtime.executor.command.CommandBuilder;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

@Service
public class MP3AudioFileChecker implements IAudioFileChecker {

	@Override
	public boolean supports(FileDescriptor fileDescriptor) {
		return AudioFormat.MP3.equals(AudioFormat.getAudioFormat(fileDescriptor).orElse(null));
	}

	@Override
	public void checkAudioFiles(Set<FileDescriptor> fileDescriptors, CheckAudioFilesOptions options, CommandLineStreams streams) throws AudioException {
		CommandBuilder cb = null;
		try {
			cb = new CommandBuilder(streams.getCommandDefinitionStream());
			
			Executor executor = new Executor();
			List<CommandVariable> params = new ArrayList<CommandVariable>();
			
			List<FileDescriptor> fileList = new ArrayList<FileDescriptor>(fileDescriptors);
			Collections.sort(fileList, new AudioFileDescriptorComparator());
			for (FileDescriptor fileDescriptor : fileList) {
				// file
				params.add(new CommandVariable("files", fileDescriptor.getFile().getAbsolutePath()));
			}
			
			// options
			if (options.isIgnoreBitrate()) {
				params.add(new CommandVariable("ignoreBitrate"));
			}
			if (options.isIgnoreCrc()) {
				params.add(new CommandVariable("ignoreCrc"));
			}
			if (options.isIgnoreEmphasis()) {
				params.add(new CommandVariable("ignoreEmphasis"));
			}
			if (options.isIgnoreSamplingFrequency()) {
				params.add(new CommandVariable("ignoreSampling"));
			}
			if (options.isIgnoreLayer()) {
				params.add(new CommandVariable("ignoreLayer"));
			}
			if (options.isIgnoreMode()) {
				params.add(new CommandVariable("ignoreMode"));
			}
			if (options.isIgnoreVersion()) {
				params.add(new CommandVariable("ignoreVersion"));
			}
			cb.createCommand(params);
			
			ExecuteRequest executeRequest = new ExecuteRequest(cb.getCommand(), streams.getLogOutputStream(), streams.getErrorOutputStream());
			executeRequest.setEnv(cb.getEnvironment());
			executeRequest.setMessage("Checking audio files...");
			executeRequest.setRedirectErrorStream(true);
			executor.executeCommand(executeRequest);
		} 
		catch (IOException exc) {
			throw new AudioException(AudioException.COMMAND_IO_ERROR, exc, cb != null ? cb.getCommandDefinition().getCommand() : null);
		}
		catch (InterruptedException exc) {
			throw new AudioException(AudioException.INTERNAL, exc);
		}
		catch (XMLStreamException exc) {
			throw new AudioException(AudioException.INTERNAL, exc);
		}
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.MP3CHECK;
	}

}
