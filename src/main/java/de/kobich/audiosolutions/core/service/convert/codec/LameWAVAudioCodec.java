package de.kobich.audiosolutions.core.service.convert.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

/**
 * Encoding service by Lame.<p>
 * Lame version: 3.98.2
 * @author ckorn
 */
@Service
@Order(value=2)
public class LameWAVAudioCodec extends AbstractCodec implements IAudioCodec {
	private static final Set<AudioFormat> SUPPORTED_INPUT_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV /*, AudioFormat.OGG*/);
	@Autowired
	private IFileID3TagService fileID3TagService;
	
	@Override
	protected List<CommandVariable> getCommandVariables(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions) throws AudioException {
		List<CommandVariable> params = new ArrayList<CommandVariable>();
		params.addAll(super.getID3TagsCommandOptions(input, fileID3TagService));
		return params;
	}
	
	@Override
	public boolean supports(FileDescriptor fileDescriptor, AudioFormat outputFormat) {
		if (AudioFormat.WAV.equals(outputFormat)) {
			return AudioFormat.getAudioFormat(fileDescriptor).stream().anyMatch(f -> SUPPORTED_INPUT_FORMATS.contains(f));
		}
		return false;
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.LAME_WAV;
	}
	
	/**
	 * @param fileID3TagService the fileID3TagService to set
	 */
	public void setFileID3TagService(IFileID3TagService fileID3TagService) {
		this.fileID3TagService = fileID3TagService;
	}

}
