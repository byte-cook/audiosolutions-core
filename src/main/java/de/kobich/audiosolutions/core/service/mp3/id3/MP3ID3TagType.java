package de.kobich.audiosolutions.core.service.mp3.id3;

/**
 * Audio data constants of mp3 id3 tags.
 */
public enum MP3ID3TagType {
	GENRE("Genre", true),
	ARTIST("Artist", true),
	ALBUM("Album", true),
	ALBUM_YEAR("Album Year", true),
	TRACK("Track", true),
	TRACK_NO("Track No", true),
	COMMENT("Comment", true),
	DURATION_SECONDS("Duration", false),
	MP3_CHANNELS("Channels", false),
	ENCODING_TYPE("EncodingType", false),
	FORMAT("Format", false),
	SAMPLE_RATE("SampleRate", false),
	MP3_BITRATE("MP3 Bitrate", false);
	
	private String label;
	private boolean editable;
	
	private MP3ID3TagType(String label, boolean editable) {
		this.label = label;
		this.editable = editable;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}
}
