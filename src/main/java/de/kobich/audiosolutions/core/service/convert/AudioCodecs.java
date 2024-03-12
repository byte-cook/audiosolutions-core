package de.kobich.audiosolutions.core.service.convert;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.convert.codec.IAudioCodec;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioCodecs {
	@Autowired
	private List<IAudioCodec> codecs;
	
	public IAudioCodec findCodec(FileDescriptor fileDescriptor, AudioFormat outputFormat) {
		for (IAudioCodec codec : codecs) {
			if (codec.supports(fileDescriptor, outputFormat)) {
				return codec;
			}
		}
		return null;
	}
	
	public IAudioCodec findCodec(CommandLineTool tool) {
		for (IAudioCodec codec : codecs) {
			if (tool.equals(codec.getCommandLineTool())) {
				return codec;
			}
		}
		return null;
	}

	public List<IAudioCodec> getCodecs() {
		return codecs;
	}
	
	/**
	 * @param codecs the codecs to set
	 */
	public void setCodecs(List<IAudioCodec> codecs) {
		this.codecs = codecs;
	}
}
