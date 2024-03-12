package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import de.kobich.commons.misc.extract.IText;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.utils.StreamUtils;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileDescriptorTextAdapter;

public class AudioServiceUtils {
	private static final Logger logger = Logger.getLogger(AudioServiceUtils.class);
	
	public static File copyInternalCommandDefinition(CommandLineTool tool, Class<?> type, File targetFile) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = tool.getInternalDefinitionStream(type);
			if (input != null) {
				FileUtils.forceMkdir(targetFile.getParentFile());
				output = new FileOutputStream(targetFile);
				IOUtils.copy(input, output);
				return targetFile;
			}
			return null;
		}
		catch (Exception exc) {
			logger.warn("Internal command definition cannot be copied", exc);
			return null;
		}
		finally {
			StreamUtils.forceClose(input);
			StreamUtils.forceClose(output);
		}
	}
	
	public static File copyInternalFile(String path, Class<?> clazz, File targetFile) {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = clazz.getResourceAsStream(path);
			if (input != null) {
				FileUtils.forceMkdir(targetFile.getParentFile());
				output = new FileOutputStream(targetFile);
				IOUtils.copy(input, output);
				return targetFile;
			}
			return null;
		}
		catch (Exception exc) {
			logger.warn("Internal command definition cannot be copied", exc);
			return null;
		}
		finally {
			StreamUtils.forceClose(input);
			StreamUtils.forceClose(output);
		}
	}
	
	/**
	 * Converts FileDescriptors to ITexts
	 * @param fileDescriptors
	 * @return
	 */
	public static Set<IText> convert2Texts(Set<FileDescriptor> fileDescriptors) {
		Set<IText> texts = new HashSet<IText>();
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			texts.add(new FileDescriptorTextAdapter(fileDescriptor));
		}
		return texts;
	}
}
