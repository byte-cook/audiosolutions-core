package de.kobich.audiosolutions.core.service.normalize.normalizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptorComparator;
import de.kobich.audiosolutions.core.service.CommandLineStreams;
import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;
import de.kobich.commons.collections.TwoDimensionalSetMap;
import de.kobich.commons.runtime.executor.ExecuteRequest;
import de.kobich.commons.runtime.executor.Executor;
import de.kobich.commons.runtime.executor.command.CommandBuilder;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

public abstract class AbstractNormalizer implements IAudioNormalizer {
	@Override
	public void execute(Set<FileDescriptor> input, IAudioNormalizationOptions normalizationOptions, CommandLineStreams streams) throws AudioException {
		CommandBuilder cb = null;
		try {
			// read command definition
			cb = new CommandBuilder(streams.getCommandDefinitionStream());

			// split file descriptors
			Executor executor = new Executor();
			Map<String, Set<FileDescriptor>> category2Files = splitFileDescriptors(input, normalizationOptions);
			List<String> categories = new ArrayList<String>(category2Files.keySet());
			Collections.sort(categories);
			for (String category : categories) {
				List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
				fileDescriptors.addAll(category2Files.get(category));
				
				List<CommandVariable> params = new ArrayList<CommandVariable>();
				params.addAll(getAdditionalParams(input, normalizationOptions));

				Collections.sort(fileDescriptors, new AudioFileDescriptorComparator());
				for (FileDescriptor fileDescriptor : fileDescriptors) {
					// source/target file
					String targetFile = fileDescriptor.getFile().getAbsolutePath();
					params.add(new CommandVariable("files", targetFile));
				}
				
				cb.createCommand(params);

				ExecuteRequest executeRequest = new ExecuteRequest(cb.getCommand(), streams.getLogOutputStream(), streams.getErrorOutputStream());
				executeRequest.setEnv(cb.getEnvironment());
				executeRequest.setMessage("Normalizing files for category: " + category);
				executeRequest.setRedirectErrorStream(true);
				executor.executeCommand(executeRequest);
			}
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
	
	/**
	 * Returns additional command parameters
	 * @param fileDescriptors
	 * @param normalizationOptions
	 * @return
	 */
	protected List<CommandVariable> getAdditionalParams(Set<FileDescriptor> fileDescriptors, IAudioNormalizationOptions normalizationOptions) {
		// no-op
		return new ArrayList<CommandVariable>();
	}
	
	/**
	 * Returns all files split by category (e.g. by album)  
	 * @param request
	 * @return
	 */
	public abstract Map<String, Set<FileDescriptor>> splitFileDescriptors(Set<FileDescriptor> fileDescriptors, IAudioNormalizationOptions normalizationOptions);
	
	protected Map<String, Set<FileDescriptor>> splitFileDescriptorsByAlbum(Set<FileDescriptor> fileDescriptors) {
		TwoDimensionalSetMap<String, FileDescriptor> category2Files = new TwoDimensionalSetMap<String, FileDescriptor>();
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			String category = "";
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
				if (audioData.hasAttribute(AudioAttribute.ARTIST)) {
					category += audioData.getAttribute(AudioAttribute.ARTIST);
				}
				if (audioData.hasAttribute(AudioAttribute.ALBUM)) {
					category += audioData.getAttribute(AudioAttribute.ALBUM);
				}
				if (audioData.hasAttribute(AudioAttribute.DISK)) {
					category += audioData.getAttribute(AudioAttribute.DISK);
				}
			}

			if (category.isEmpty()) {
				category = fileDescriptor.getFile().getParentFile().getAbsolutePath();
			}
			
			category2Files.addElement(category, fileDescriptor);
		}
		return category2Files;
	}
	
	protected Map<String, Set<FileDescriptor>> splitFileDescriptorsByTrack(Set<FileDescriptor> fileDescriptors) {
		TwoDimensionalSetMap<String, FileDescriptor> category2Files = new TwoDimensionalSetMap<String, FileDescriptor>();
		category2Files.addAllElements("?", fileDescriptors);
		return category2Files;
	}

}
