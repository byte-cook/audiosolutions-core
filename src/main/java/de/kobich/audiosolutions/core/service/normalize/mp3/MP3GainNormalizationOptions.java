package de.kobich.audiosolutions.core.service.normalize.mp3;

import java.io.InputStream;

import de.kobich.audiosolutions.core.service.normalize.IAudioNormalizationOptions;

public class MP3GainNormalizationOptions implements IAudioNormalizationOptions {
	public static enum AudioNormalizingMode {
		TRACK_GAIN, ALBUM_GAIN
	}
	private final AudioNormalizingMode mode;
	private final float suggestedDecibel;
	private boolean lowerGainInsteadOfClipping;
	private InputStream commandDefinitionStream;
	
	/**
	 * @param mode
	 * @param suggestedDecibel
	 */
	public MP3GainNormalizationOptions(AudioNormalizingMode mode, float suggestedDecibel) {
		this.mode = mode;
		this.suggestedDecibel = suggestedDecibel;
	}

	/**
	 * @return the lowerGainInsteadOfClipping
	 */
	public boolean isLowerGainInsteadOfClipping() {
		return lowerGainInsteadOfClipping;
	}

	/**
	 * @param lowerGainInsteadOfClipping the lowerGainInsteadOfClipping to set
	 */
	public void setLowerGainInsteadOfClipping(boolean lowerGainInsteadOfClipping) {
		this.lowerGainInsteadOfClipping = lowerGainInsteadOfClipping;
	}

	/**
	 * @return the commandDefinitionStream
	 */
	public InputStream getCommandDefinitionStream() {
		return commandDefinitionStream;
	}

	/**
	 * @param commandDefinitionStream the commandDefinitionStream to set
	 */
	public void setCommandDefinitionStream(InputStream commandDefinitionStream) {
		this.commandDefinitionStream = commandDefinitionStream;
	}

	/**
	 * @return the mode
	 */
	public AudioNormalizingMode getMode() {
		return mode;
	}

	/**
	 * @return the suggestedDecibel
	 */
	public float getSuggestedDecibel() {
		return suggestedDecibel;
	}

}
