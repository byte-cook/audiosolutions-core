package de.kobich.audiosolutions.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AudioDataBuilder {
	private String medium;
	private boolean mediumRemove;

	private String artist;
	private boolean artistRemove;
	
	private String genre;
	private boolean genreRemove;
	
	private String album;
	private boolean albumRemove;
	
	private Date albumPublication;
	private boolean albumPublicationRemove;
	
	private String disk;
	private boolean diskRemove;
	
	private String track;
	private boolean trackRemove;
	
	private Integer trackNo;
	private boolean trackNoRemove;
	
	private String trackFormat;
	private boolean trackFormatRemove;
	
	private RatingType rating;
	private boolean ratingRemove;

	public static AudioDataBuilder builder() {
		return new AudioDataBuilder();
	}

	public Map<AudioAttribute, String> buildMap() {
		Map<AudioAttribute, String> audioDataValues = new HashMap<>();
		if (medium != null) {
			audioDataValues.put(AudioAttribute.MEDIUM, medium);
		}
		else if (mediumRemove) {
			audioDataValues.put(AudioAttribute.MEDIUM, null);
		}
		
		if (artist != null) {
			audioDataValues.put(AudioAttribute.ARTIST, artist);
		}
		else if (artistRemove) {
			audioDataValues.put(AudioAttribute.ARTIST, null);
		}
		
		if (genre != null) {
			audioDataValues.put(AudioAttribute.GENRE, genre);
		}
		else if (genreRemove) {
			audioDataValues.put(AudioAttribute.GENRE, null);
		}
		
		if (album != null) {
			audioDataValues.put(AudioAttribute.ALBUM, album);
		}
		else if (albumRemove) {
			audioDataValues.put(AudioAttribute.ALBUM, null);
		}
		
		if (albumPublication != null) {
			audioDataValues.put(AudioAttribute.ALBUM_PUBLICATION, AudioAttributeUtils.convert2String(albumPublication));
		}
		else if (albumPublicationRemove) {
			audioDataValues.put(AudioAttribute.ALBUM_PUBLICATION, null);
		}
		
		if (disk != null) {
			audioDataValues.put(AudioAttribute.DISK, disk);
		}
		else if (diskRemove) {
			audioDataValues.put(AudioAttribute.DISK, null);
		}
		
		if (track != null) {
			audioDataValues.put(AudioAttribute.TRACK, track);
		}
		else if (trackRemove) {
			audioDataValues.put(AudioAttribute.TRACK, null);
		}
		
		if (trackNo != null) {
			audioDataValues.put(AudioAttribute.TRACK_NO, String.valueOf(trackNo));
		}
		else if (trackNoRemove) {
			audioDataValues.put(AudioAttribute.TRACK_NO, null);
		}
		
		if (trackFormat != null) {
			audioDataValues.put(AudioAttribute.TRACK_FORMAT, trackFormat);
		}
		else if (trackFormatRemove) {
			audioDataValues.put(AudioAttribute.TRACK_FORMAT, null);
		}
		
		if (rating != null) {
			audioDataValues.put(AudioAttribute.RATING, rating.name());
		}
		else if (ratingRemove) {
			audioDataValues.put(AudioAttribute.RATING, null);
		}
		return audioDataValues;
	}

	public AudioData build() {
		Map<AudioAttribute, String> audioDataValues = buildMap();
		return new AudioData(audioDataValues);
	}

}
