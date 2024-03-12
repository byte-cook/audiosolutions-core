package de.kobich.audiosolutions.core.service.imexport.metadata;

public enum AudioCSVTokenType {
	AUDIO_DATA("Audiodata"),
	STATE("State"),
	MEDIUM("Medium"), 
	ARTIST("Artist"), 
	ALBUM("Album"), 
	ALBUM_PUBLICATION("AlbumPublication"), 
	DISK("Disk"), 
	TRACK("Track"), 
	TRACK_FORMAT("TrackFormat"), 
	TRACK_NO("TrackNo"), 
	GENRE("Genre"), 
	RATING("Rating");
	
	private String name;
	
	private AudioCSVTokenType(String name) {
		this.name = name;
	}
	
	public String tag() {
		return name;
	}
}
