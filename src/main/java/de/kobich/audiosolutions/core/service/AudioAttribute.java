package de.kobich.audiosolutions.core.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Audio meta data attributes.<p>
 * Note: Adding new types requires:<br>
 * - update AudioAttributeComparator<br>
 * - update AudioPersistenceService<br>
 * - update AudioDataService<br>
 * - update AudioSearchService<br>
 * - update AudioAttributeUtils if a new type is required<br>
 * @author ckorn
 * @see AudioAttributeComparator
 */
public enum AudioAttribute {
	MEDIUM(true, String.class), 
	ARTIST(true, String.class), 
	ALBUM(true, String.class), 
	ALBUM_PUBLICATION(false, Date.class), 
	DISK(false, String.class), 
	TRACK(true, String.class), 
	TRACK_NO(false, Integer.class), 
	TRACK_FORMAT(false, String.class), 
	GENRE(true, String.class), 
	RATING(false, RatingType.class);
	
	private final boolean required;
	private final Class<?> type;
	private static AudioAttribute[] requiredAttributes;

	private AudioAttribute(boolean required, Class<?> type) {
		this.required = required;
		this.type = type;
	}

	/**
	 * Indicates if this attribute is required
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Returns all required attributes
	 * @return
	 */
	public static AudioAttribute[] getRequiredAttributes() {
		if (requiredAttributes == null) {
			List<AudioAttribute> requiredAttributeList = new ArrayList<AudioAttribute>();
			for (AudioAttribute attribute : AudioAttribute.values()) {
				if (attribute.isRequired()) {
					requiredAttributeList.add(attribute);
				}
			}
			requiredAttributes = requiredAttributeList.toArray(new AudioAttribute[requiredAttributeList.size()]);
		}
		return requiredAttributes;
	}
}
