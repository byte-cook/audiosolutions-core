package de.kobich.audiosolutions.core.service.convert;

import de.kobich.commons.Reject;

public class LameMP3ConversionOptions implements IAudioConversionOptions {
	public static final int DEFAULT_VBR_QUALITY = 9;
	public static final int NOT_SPECIFIED = -1;
	public static enum AudioEncodingMode {
		CONSTANT_BITRATE, //--cbr 
		AVERAGE_BITRATE, // -abr 
		VARIABLE_BITRATE // -vbr
	}
	private final AudioEncodingMode encodingMode;
	private boolean mono;
	/**
	 * -b 8-320
	 */
	private int cbrBitrate;
	/**
	 * --abr 8-320
	 */
	private int abrBitrate;
	/**
	 * Quality is between [1, 10]. Higher values mean higher quality.
	 */
	private int vbrQuality; 
	private int maxBitrate;
	private int minBitrate;
	
	public LameMP3ConversionOptions(int cbrBitrate) {
		this.encodingMode = AudioEncodingMode.CONSTANT_BITRATE;
		this.mono = false;
		Reject.ifTrue(cbrBitrate < 8 || 320 < cbrBitrate, "CBR bitrate must be between 8 and 320");
		this.cbrBitrate = cbrBitrate;
	}

	public LameMP3ConversionOptions(int abrBitrate, int maxBitrate) {
		this.encodingMode = AudioEncodingMode.AVERAGE_BITRATE;
		this.mono = false;
		Reject.ifTrue(abrBitrate < 8 || 320 < abrBitrate, "ABR bitrate must be between 8 and 320");
		this.abrBitrate = abrBitrate;
		this.maxBitrate = maxBitrate;
	}
	
	public LameMP3ConversionOptions(int vbrQuality, int minBitrate, int maxBitrate) {
		this.encodingMode = AudioEncodingMode.VARIABLE_BITRATE;
		this.mono = false;
		Reject.ifTrue(vbrQuality < 1 || 10 < vbrQuality, "VBR Quality must be between 1 and 10");
		this.vbrQuality = vbrQuality;
		this.minBitrate = minBitrate;
		this.maxBitrate = maxBitrate;
	}

	/**
	 * @return the encodingMode
	 */
	public AudioEncodingMode getEncodingMode() {
		return encodingMode;
	}

	/**
	 * @return the mono
	 */
	public boolean isMono() {
		return mono;
	}

	/**
	 * @param mono the mono to set
	 */
	public void setMono(boolean mono) {
		this.mono = mono;
	}

	/**
	 * @return the cbrBitrate
	 */
	public int getCBRBitrate() {
		return cbrBitrate;
	}

	/**
	 * @return the abrBitrate
	 */
	public int getABRBitrate() {
		return abrBitrate;
	}

	/**
	 * @return the vbrQuality
	 */
	public int getVBRQuality() {
		return vbrQuality;
	}

	/**
	 * @return the minQuality
	 */
	public int getMinBitrate() {
		return minBitrate;
	}

	/**
	 * @return the maxBitrate
	 */
	public int getMaxBitrate() {
		return maxBitrate;
	}

}
