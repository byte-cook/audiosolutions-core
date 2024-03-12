package de.kobich.audiosolutions.core.service.check;

public class CheckAudioFilesOptions {
	private boolean ignoreCrc;
	private boolean ignoreMode;
	private boolean ignoreLayer;
	private boolean ignoreBitrate;
	private boolean ignoreVersion;
	private boolean ignoreSamplingFrequency;
	private boolean ignoreEmphasis;
	
	public CheckAudioFilesOptions() {
		this.ignoreBitrate = false;
		this.ignoreCrc = false;
		this.ignoreEmphasis = false;
		this.ignoreSamplingFrequency = false;
		this.ignoreLayer = false;
		this.ignoreMode = false;
		this.ignoreVersion = false;
	}

	public boolean isIgnoreCrc() {
		return ignoreCrc;
	}

	public void setIgnoreCrc(boolean crc) {
		this.ignoreCrc = crc;
	}

	public boolean isIgnoreMode() {
		return ignoreMode;
	}

	public void setIgnoreMode(boolean mode) {
		this.ignoreMode = mode;
	}

	public boolean isIgnoreLayer() {
		return ignoreLayer;
	}

	public void setIgnoreLayer(boolean layer) {
		this.ignoreLayer = layer;
	}

	public boolean isIgnoreBitrate() {
		return ignoreBitrate;
	}

	public void setIgnoreBitrate(boolean bitrate) {
		this.ignoreBitrate = bitrate;
	}

	public boolean isIgnoreVersion() {
		return ignoreVersion;
	}

	public void setIgnoreVersion(boolean version) {
		this.ignoreVersion = version;
	}

	public boolean isIgnoreSamplingFrequency() {
		return ignoreSamplingFrequency;
	}

	public void setIgnoreSamplingFrequency(boolean samplingFrequency) {
		this.ignoreSamplingFrequency = samplingFrequency;
	}

	public boolean isIgnoreEmphasis() {
		return ignoreEmphasis;
	}

	public void setIgnoreEmphasis(boolean emphasis) {
		this.ignoreEmphasis = emphasis;
	}

}
