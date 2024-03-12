package de.kobich.audiosolutions.core.service.convert.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions;
import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions.AudioEncodingMode;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.commons.Reject;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

/**
 * Encoding service by Lame.<p>
 * Lame version: 3.98.2
 * @author ckorn
 */
@Service
@Order(value=1)
public class LameMP3AudioCodec extends AbstractCodec implements IAudioCodec {
	private static final Set<AudioFormat> SUPPORTED_INPUT_FORMATS = Set.of(AudioFormat.MP3, AudioFormat.WAV /*, AudioFormat.OGG*/);
	private static final Map<Integer, String> VBR_QUALITY_MAPPING = Map.of(1, "9", 2, "8", 3, "7", 4, "6", 5, "5", 6, "4", 7, "3", 8, "2", 9, "1", 10, "0");

	@Autowired
	private IFileID3TagService fileID3TagService;
	
	@Override
	protected List<CommandVariable> getCommandVariables(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions) throws AudioException {
		Reject.ifFalse(convertionOptions instanceof LameMP3ConversionOptions, "Illegal option type");
		LameMP3ConversionOptions mp3Options = (LameMP3ConversionOptions) convertionOptions;
		
		List<CommandVariable> params = new ArrayList<CommandVariable>();
		params.addAll(getLameCommand(mp3Options));
		params.addAll(super.getID3TagsCommandOptions(input, fileID3TagService));
		return params;
	}
	
	/**
	 * Returns the Lame command
	 * @param request
	 * @return
	 */
	private List<CommandVariable> getLameCommand(LameMP3ConversionOptions mp3Options) {
		List<CommandVariable> params = new ArrayList<CommandVariable>();
		if (mp3Options.isMono()) {
			params.add(new CommandVariable("mode.mono"));
		}
		
		AudioEncodingMode mode = mp3Options.getEncodingMode();
		if (AudioEncodingMode.CONSTANT_BITRATE.equals(mode)) {
			params.add(new CommandVariable("bitrate.constant", "" + mp3Options.getCBRBitrate()));
		}
		else if (AudioEncodingMode.AVERAGE_BITRATE.equals(mode)) {
			params.add(new CommandVariable("bitrate.average", "" + mp3Options.getABRBitrate()));
			if (LameMP3ConversionOptions.NOT_SPECIFIED != mp3Options.getMaxBitrate()) {
				params.add(new CommandVariable("bitrate.average.max", "" + mp3Options.getMaxBitrate()));
			}
		}
		else if (AudioEncodingMode.VARIABLE_BITRATE.equals(mode)) {
			int quality = mp3Options.getVBRQuality();
			Reject.ifTrue(quality < 1 || 10 < quality, "Quality out of bounds: " + quality);
			String qualityStr = VBR_QUALITY_MAPPING.get(quality);
			Reject.ifNull(qualityStr, "Unkown quality: " + quality);
			params.add(new CommandVariable("bitrate.variable", qualityStr));
			if (LameMP3ConversionOptions.NOT_SPECIFIED != mp3Options.getMinBitrate()) {
				params.add(new CommandVariable("bitrate.variable.min", "" + mp3Options.getMinBitrate()));
			}
			if (LameMP3ConversionOptions.NOT_SPECIFIED != mp3Options.getMaxBitrate()) {
				params.add(new CommandVariable("bitrate.variable.max", "" + mp3Options.getMaxBitrate()));
			}
		}
		return params;
	}
	
	@Override
	public boolean supports(FileDescriptor fileDescriptor, AudioFormat outputFormat) {
		if (AudioFormat.MP3.equals(outputFormat)) {
			return AudioFormat.getAudioFormat(fileDescriptor).stream().anyMatch(f -> SUPPORTED_INPUT_FORMATS.contains(f));
		}
		return false;
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.LAME_MP3;
	}

	/**
	 * @param fileID3TagService the fileID3TagService to set
	 */
	public void setFileID3TagService(IFileID3TagService fileID3TagService) {
		this.fileID3TagService = fileID3TagService;
	}
}
