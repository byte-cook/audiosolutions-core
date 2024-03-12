package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.kobich.component.file.FileDescriptor;

public class AudioFormatUtil {
	
	
	public static FileDescriptor getOutputFile(FileDescriptor fileDescriptor, File outputDir, AudioFormat outputFormat) throws IOException {
		String fileBaseName = FilenameUtils.getBaseName(fileDescriptor.getFileName());
		String fileName = fileBaseName + FilenameUtils.EXTENSION_SEPARATOR + outputFormat.getExtension();
		if (outputDir == null) {
			File parentFile = fileDescriptor.getFile().getParentFile();
			File output = new File(parentFile, fileName);
			return new FileDescriptor(output, parentFile);
		}
		else {
			FileUtils.forceMkdir(outputDir);
			File output = new File(outputDir, fileName);
			return new FileDescriptor(output, outputDir);
		}
	}
}
