package de.kobich.audiosolutions.core.service.normalize;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.normalize.normalizer.IAudioNormalizer;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioNormalizers {
	@Autowired
	private List<IAudioNormalizer> normalizers;
	
	public IAudioNormalizer findNormalizer(FileDescriptor fileDescriptor) {
		for (IAudioNormalizer n : normalizers) {
			if (n.supports(fileDescriptor)) {
				return n;
			}
		}
		return null;
	}
	
	public IAudioNormalizer findNormalizer(CommandLineTool tool) {
		for (IAudioNormalizer n : normalizers) {
			if (tool.equals(n.getCommandLineTool())) {
				return n;
			}
		}
		return null;
	}

	/**
	 * @return the normalizers
	 */
	public List<IAudioNormalizer> getNormalizers() {
		return normalizers;
	}

	/**
	 * @param normalizers the normalizers to set
	 */
	public void setNormalizers(List<IAudioNormalizer> normalizers) {
		this.normalizers = normalizers;
	}
}
