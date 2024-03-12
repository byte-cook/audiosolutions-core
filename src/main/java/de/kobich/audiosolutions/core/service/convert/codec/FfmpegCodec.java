package de.kobich.audiosolutions.core.service.convert.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.convert.FfmpegConversionOptions;
import de.kobich.audiosolutions.core.service.convert.IAudioConversionOptions;
import de.kobich.commons.Reject;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

@Service
@Order(value=3)
public class FfmpegCodec extends AbstractCodec implements IAudioCodec {
	
	private static Map<AudioFormat, CodecSettings> CODECS = Map.of(AudioFormat.MP3, CodecSettings.CODEC_MP3, AudioFormat.OGG, CodecSettings.CODEC_OGG,
			AudioFormat.AAC, CodecSettings.CODEC_AAC);
	
	private enum CodecSettings {
		CODEC_MP3("libmp3lame", Map.of(1, "9", 2, "8", 3, "7", 4, "6", 5, "5", 6, "4", 7, "3", 8, "2", 9, "1", 10, "0")),
		CODEC_OGG("libvorbis", Map.of(1, "1", 2, "2", 3, "3", 4, "4", 5, "5", 6, "6", 7, "7", 8, "8", 9, "9", 10, "10")),
		CODEC_AAC("libfdk_aac", Map.of(1, "1", 2, "1", 3, "2", 4, "2", 5, "3", 6, "3", 7, "4", 8, "4", 9, "5", 10, "5")),
		;
		
		private CodecSettings(String name, Map<Integer, String> qualityMapping) {
			this.name = name;
			this.qualityMapping = qualityMapping; 
		}
		
		private final String name;
		/**
		 * Integer: selected quality in UI
		 * String: command line option value
		 */
		private final Map<Integer, String> qualityMapping;
		
	}

	@Override
	protected List<CommandVariable> getCommandVariables(FileDescriptor input, FileDescriptor output, AudioFormat outputFormat, IAudioConversionOptions convertionOptions) throws AudioException {
		Reject.ifFalse(convertionOptions instanceof FfmpegConversionOptions, "Illegal option type");
		FfmpegConversionOptions ffmpegOptions = (FfmpegConversionOptions) convertionOptions;
		
		List<CommandVariable> params = new ArrayList<CommandVariable>();
		// codec
		CodecSettings codec = CODECS.get(outputFormat);
		if (codec != null) {
			params.add(new CommandVariable("codec", codec.name));

			// quality
			int quality = ffmpegOptions.getQuality();
			Reject.ifTrue(quality < 1 || 10 < quality, "Quality out of bounds: " + quality);
			switch (codec) {
			case CODEC_AAC:
				// https://trac.ffmpeg.org/wiki/Encode/AAC
				// VBR: Target a quality, rather than a specific bit rate. 1 is lowest quality and 5 is highest quality.
				params.add(new CommandVariable("quality.aac", codec.qualityMapping.get(quality)));
				break;
			case CODEC_MP3:
				// https://trac.ffmpeg.org/wiki/Encode/MP3
				// for libmp3lame the range is 0-9 where a lower value is a higher quality. 
				params.add(new CommandVariable("quality", "" + codec.qualityMapping.get(quality)));
				break;
			case CODEC_OGG:
				// https://trac.ffmpeg.org/wiki/TheoraVorbisEncodingGuide
				// Range is -1.0 to 10.0, where 10.0 is highest quality. Default is -q:a 3 with a target of ​112kbps. 
				// The formula 16×(q+4) is used below 4, 32×q is used below 8, and 64×(q-4) otherwise. Examples: 112=16×(3+4), 160=32×5, 200=32×6.25, 384=64×(10-4). 
				params.add(new CommandVariable("quality", codec.qualityMapping.get(quality)));
				break;
			}
		}
		
		return params;
	}

	@Override
	public boolean supports(FileDescriptor fileDescriptor, AudioFormat outputFormat) {
		// ffmpeg supports nearly all formats :)
		return true;
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.FFMPEG;
	}

}
