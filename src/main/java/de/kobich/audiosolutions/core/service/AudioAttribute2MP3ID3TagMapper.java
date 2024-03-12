package de.kobich.audiosolutions.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;

/**
 * Maps audio attributes to structure variables.
 * @author ckorn
 */
public class AudioAttribute2MP3ID3TagMapper {
	private static AudioAttribute2MP3ID3TagMapper instance;
	private Map<MP3ID3TagType, AudioAttribute> map;
	private Collection<MP3ID3TagType> variables;
	
	/**
	 * Returns the instance
	 * @return
	 */
	public static final AudioAttribute2MP3ID3TagMapper getInstance() {
		if (instance == null) {
			instance = new AudioAttribute2MP3ID3TagMapper();
		}
		return instance;
	}
	
	/**
	 * Constructor
	 */
	private AudioAttribute2MP3ID3TagMapper() {
		this.map = new Hashtable<MP3ID3TagType, AudioAttribute>();
		map.put(MP3ID3TagType.ALBUM, AudioAttribute.ALBUM);
		map.put(MP3ID3TagType.ALBUM_YEAR, AudioAttribute.ALBUM_PUBLICATION);
		map.put(MP3ID3TagType.ARTIST, AudioAttribute.ARTIST);
		map.put(MP3ID3TagType.GENRE, AudioAttribute.GENRE);
		map.put(MP3ID3TagType.TRACK, AudioAttribute.TRACK);
		map.put(MP3ID3TagType.TRACK_NO, AudioAttribute.TRACK_NO);
		map.put(MP3ID3TagType.ENCODING_TYPE, AudioAttribute.TRACK_FORMAT);
		
		variables = new ArrayList<MP3ID3TagType>(map.keySet());
	}
	
	/**
	 * Returns all structure variables
	 * @return
	 */
	public Collection<MP3ID3TagType> getVariables() {
		return variables;
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
	 * Returns the audio attribute for a variable
	 * @param variable
	 * @return
	 */
	public AudioAttribute getAudioAttribute(MP3ID3TagType variable) {
		return map.get(variable);
	}
}
