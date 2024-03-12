package de.kobich.audiosolutions.core.service.normalize.normalizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioFormat;
import de.kobich.audiosolutions.core.service.AudioTool;
import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;
import de.kobich.audiosolutions.core.service.normalize.mp3.MP3GainNormalizationOptions;
import de.kobich.audiosolutions.core.service.normalize.mp3.MP3GainNormalizationOptions.AudioNormalizingMode;
import de.kobich.commons.Reject;
import de.kobich.commons.runtime.executor.command.CommandLineTool;
import de.kobich.commons.runtime.executor.command.CommandVariable;
import de.kobich.component.file.FileDescriptor;

/**
 * MP3gain normalizer
 * Example for album gain 90 dB: mp3gain.exe /q /p /c /a /k /d 1.0 "track1.mp3" "track2.mp3"
 */
@Service
public class MP3GainNormalizer extends AbstractNormalizer implements IAudioNormalizer {
	public static final float DEFAULT_GAIN = 89.0F;

	@Override
	public Map<String, Set<FileDescriptor>> splitFileDescriptors(Set<FileDescriptor> fileDescriptors,
			IAudioNormalizationOptions normalizationOptions) {
		Reject.ifFalse(normalizationOptions instanceof MP3GainNormalizationOptions, "Illegal request type");
		MP3GainNormalizationOptions mp3GainOptions = (MP3GainNormalizationOptions) normalizationOptions;
		// album gain
		if (AudioNormalizingMode.ALBUM_GAIN.equals(mp3GainOptions.getMode())) {
			return splitFileDescriptorsByAlbum(fileDescriptors);
		} 
		// track gain
		else if (AudioNormalizingMode.TRACK_GAIN.equals(mp3GainOptions.getMode())) {
			return splitFileDescriptorsByTrack(fileDescriptors);
		}
		return new HashMap<String, Set<FileDescriptor>>();
	}
	
	@Override
	public List<CommandVariable> getAdditionalParams(Set<FileDescriptor> fileDescriptors, IAudioNormalizationOptions normalizationOptions) {
		Reject.ifFalse(normalizationOptions instanceof MP3GainNormalizationOptions, "Illegal request type");
		MP3GainNormalizationOptions mp3GainOptions = (MP3GainNormalizationOptions) normalizationOptions;

		List<CommandVariable> params = new ArrayList<CommandVariable>();
		if (mp3GainOptions.isLowerGainInsteadOfClipping()) {
			params.add(new CommandVariable("avoidclipping"));
		}
		// determine gain (base is default)
		float gain = mp3GainOptions.getSuggestedDecibel() - DEFAULT_GAIN;
		gain = (float) (Math.round(gain*100.0) / 100.0F);
		params.add(new CommandVariable("gain", "" + gain));
		
		// album gain
		if (AudioNormalizingMode.ALBUM_GAIN.equals(mp3GainOptions.getMode())) {
			params.add(new CommandVariable("gain.album"));
		} 
		// track gain
		else if (AudioNormalizingMode.TRACK_GAIN.equals(mp3GainOptions.getMode())) {
			params.add(new CommandVariable("gain.track"));
		}
		return params;
	}

	@Override
	public boolean supports(FileDescriptor fileDescriptor) {
		return AudioFormat.MP3.equals(AudioFormat.getAudioFormat(fileDescriptor).orElse(null));
	}

	@Override
	public CommandLineTool getCommandLineTool() {
		return AudioTool.MP3GAIN;
	}

}
