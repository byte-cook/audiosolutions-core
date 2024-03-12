package de.kobich.audiosolutions.core.service.convert;

import de.kobich.commons.Reject;

public class FfmpegConversionOptions implements IAudioConversionOptions {
	public static final int DEFAULT_QUALITY = 8;
	/**
	 * Quality is between [1, 10]. Higher values mean higher quality.
	 * The quality will be set depending on the codec.
	 */
	private final int quality;
	
	public FfmpegConversionOptions() {
		this(DEFAULT_QUALITY);
	}
	
	public FfmpegConversionOptions(int quality) {
		Reject.ifTrue(quality < 1 || 10 < quality, "Quality must be between 1 and 10");
		this.quality = quality;
	}

	public int getQuality() {
		return quality;
	}
	
}
