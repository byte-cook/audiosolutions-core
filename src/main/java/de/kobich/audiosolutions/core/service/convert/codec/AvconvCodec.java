package de.kobich.audiosolutions.core.service.convert.codec;

import java.util.List;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

//not used
@Deprecated // replaced by ffmpeg
public class AvconvCodec extends AbstractCodec implements IAudioCodec {
//	@Inject
//	private IFileID3TagService fileID3TagService;

	@Override
	protected List<CommandVariable> getCommandVariables(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions) throws AudioException {
//		List<CommandVariable> params = new ArrayList<CommandVariable>();
//		params.addAll(super.getID3TagsCommandOptions(input, fileID3TagService));
//		return params;
		return super.getCommandVariables(input, output, outputFormat, convertionOptions);
	}

	@Override
	public boolean supports(FileDescriptor fileDescriptor, AudioFormat outputFormat) {
		return AudioFormat.getAudioFormat(fileDescriptor).isPresent();
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.AVCONV;
	}

}
