package de.kobich.audiosolutions.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import de.kobich.commons.misc.extract.StructureVariable;

/**
 * Maps audio attributes to structure variables.
 * @author ckorn
 */
public class AudioAttribute2StructureVariableMapper {
	private static AudioAttribute2StructureVariableMapper instance;
	public static final String ALBUM_VAR = "<album>";
	public static final String ALBUM_YEAR_VAR = "<albumYear>";
	public static final String ARTIST_VAR = "<artist>";
	public static final String DISK_VAR = "<disk>";
	public static final String GENRE_VAR = "<genre>";
	public static final String MEDIUM_VAR = "<medium>";
	public static final String TRACK_VAR = "<track>";
	public static final String TRACK_FORMAT_VAR = "<trackFormat>";
	public static final String TRACK_NO_VAR = "<trackNo>";
	private Map<StructureVariable, AudioAttribute> map;
	private Collection<StructureVariable> variables;
	
	/**
	 * Returns the instance
	 * @return
	 */
	public static final AudioAttribute2StructureVariableMapper getInstance() {
		if (instance == null) {
			instance = new AudioAttribute2StructureVariableMapper();
		}
		return instance;
	}
	
	/**
	 * Constructor
	 */
	private AudioAttribute2StructureVariableMapper() {
		this.map = new Hashtable<StructureVariable, AudioAttribute>();
		map.put(new StructureVariable(ALBUM_VAR), AudioAttribute.ALBUM);
		map.put(new StructureVariable(ALBUM_YEAR_VAR, "(\\\\d*)", "\\\\d*"), AudioAttribute.ALBUM_PUBLICATION);
		map.put(new StructureVariable(ARTIST_VAR), AudioAttribute.ARTIST);
		map.put(new StructureVariable(DISK_VAR), AudioAttribute.DISK);
		map.put(new StructureVariable(GENRE_VAR), AudioAttribute.GENRE);
		map.put(new StructureVariable(MEDIUM_VAR), AudioAttribute.MEDIUM);
		map.put(new StructureVariable(TRACK_VAR), AudioAttribute.TRACK);
		map.put(new StructureVariable(TRACK_FORMAT_VAR), AudioAttribute.TRACK_FORMAT);
		map.put(new StructureVariable(TRACK_NO_VAR, "(\\\\d*)", "\\\\d*"), AudioAttribute.TRACK_NO);
		
		variables = new ArrayList<StructureVariable>(map.keySet());
		variables.add(new StructureVariable("<>"));
	}
	
	/**
	 * Returns all structure variables
	 * @return
	 */
	public Collection<StructureVariable> getVariables() {
		return variables;
	}
	
	/**
	 * Returns the comparator of structure variables
	 * @return
	 */
	public Comparator<StructureVariable> getVariableComparator() {
		return new Comparator<StructureVariable>() {
			public int compare(StructureVariable o1, StructureVariable o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};
	}
	
	/**
	 * Returns the audio attribute for a variable
	 * @param variable
	 * @return
	 */
	public AudioAttribute getAudioAttribute(StructureVariable variable) {
		return map.get(variable);
	}

	/**
	 * @return the map
	 */
	public Map<StructureVariable, AudioAttribute> getMap() {
		return Collections.unmodifiableMap(map);
	}
}
