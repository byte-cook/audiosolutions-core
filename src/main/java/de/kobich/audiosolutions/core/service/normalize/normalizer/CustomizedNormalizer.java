package de.kobich.audiosolutions.core.service.normalize.normalizer;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

// not used
@Deprecated
public class CustomizedNormalizer extends AbstractNormalizer implements IAudioNormalizer {
	@Autowired
	private File commandDefinitionDirectory;

	@Override
	public boolean supports(FileDescriptor fileDescriptor) {
		CommandLineTool tool = getCommandLineTool();
		return new File(commandDefinitionDirectory, tool.getFileName()).exists();
	}

	@Override
	public CommandLineTool getCommandLineTool() {
//		String baseName = format.getExtension();
//		return new CommandLineTool("Customized " + baseName.toUpperCase(), "", baseName);
		return AudioTool.UNKNOWN;
	}

	@Override
	public Map<String, Set<FileDescriptor>> splitFileDescriptors(Set<FileDescriptor> fileDescriptors,
			IAudioNormalizationOptions normalizationOptions) {
		return super.splitFileDescriptorsByTrack(fileDescriptors);
	}

}
