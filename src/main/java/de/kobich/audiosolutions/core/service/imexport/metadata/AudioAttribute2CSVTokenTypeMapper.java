package de.kobich.audiosolutions.core.service.imexport.metadata;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;

/**
 * Maps audio attributes to structure variables.
 * @author ckorn
 */
public class AudioAttribute2CSVTokenTypeMapper {
	private static AudioAttribute2CSVTokenTypeMapper instance;
	private Map<AudioAttribute, AudioCSVTokenType> map;
	
	/**
	 * Returns the instance
	 * @return
	 */
	public static final AudioAttribute2CSVTokenTypeMapper getInstance() {
		if (instance == null) {
			instance = new AudioAttribute2CSVTokenTypeMapper();
		}
		return instance;
	}
	
	/**
	 * Constructor
	 */
	private AudioAttribute2CSVTokenTypeMapper() {
		this.map = new Hashtable<AudioAttribute, AudioCSVTokenType>();
		map.put(AudioAttribute.ALBUM, AudioCSVTokenType.ALBUM);
		map.put(AudioAttribute.ALBUM_PUBLICATION, AudioCSVTokenType.ALBUM_PUBLICATION);
		map.put(AudioAttribute.ARTIST, AudioCSVTokenType.ARTIST);
		map.put(AudioAttribute.DISK, AudioCSVTokenType.DISK);
		map.put(AudioAttribute.GENRE, AudioCSVTokenType.GENRE);
		map.put(AudioAttribute.MEDIUM, AudioCSVTokenType.MEDIUM);
		map.put(AudioAttribute.RATING, AudioCSVTokenType.RATING);
		map.put(AudioAttribute.TRACK, AudioCSVTokenType.TRACK);
		map.put(AudioAttribute.TRACK_FORMAT, AudioCSVTokenType.TRACK_FORMAT);
		map.put(AudioAttribute.TRACK_NO, AudioCSVTokenType.TRACK_NO);
	}
	
	/**
	 * Returns the comparator of id3 tags
	 * @return
	 */
	public Comparator<MP3ID3TagType> getVariableComparator() {
		return new Comparator<MP3ID3TagType>() {
			public int compare(MP3ID3TagType o1, MP3ID3TagType o2) {
				return o1.name().compareTo(o2.name());
			}
		};
	}
	
	/**
	 * Returns the audio csv token for an attribute
	 * @param attribute
	 * @return
	 */
	public AudioCSVTokenType getCSVTokenType(AudioAttribute attribute) {
		return map.get(attribute);
	}
}
