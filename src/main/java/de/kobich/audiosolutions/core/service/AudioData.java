package de.kobich.audiosolutions.core.service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.commons.utils.CloneUtils;
import de.kobich.component.file.IMetaData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Audio meta data.
 * @author ckorn
 */
public class AudioData implements IMetaData {
	private static final long serialVersionUID = -5576295402575400174L;
	private static final Logger logger = Logger.getLogger(AudioData.class);
	public static final String TRACK_ID_PROP = "trackId";
	public static final String AUDIO_ATTRIBUTE_PROP = "audioAttribute";
	public static final String AUDIO_STATE_PROP = "audioState";
	public static final String DEFAULT_VALUE = "?";
	private PropertyChangeSupport support;
	private Long trackId;
	private Map<AudioAttribute, String> attributes;
	@Getter
	private AudioState state;
	
	@Setter
	private AlbumIdentity albumIdentity;
	@Setter(value = AccessLevel.PRIVATE)
	private String artistDescription;
	@Setter(value = AccessLevel.PRIVATE)
	private String albumDescription;
	@Setter(value = AccessLevel.PRIVATE)
	private String trackDescription;
	
	public AudioData() {
		this.attributes = new Hashtable<AudioAttribute, String>();
		this.support = new PropertyChangeSupport(this);
		this.state = AudioState.TRANSIENT_INCOMPLETE;
	}
	public AudioData(Track track) {
		this();
		// album
		this.setAttribute(AudioAttribute.ALBUM, track.getAlbum().getName());
		this.setAlbumDescription(track.getAlbum().getDescription());
		if (track.getAlbum().getPublication() != null) {
			String dateText = AudioAttributeUtils.convert2String(track.getAlbum().getPublication());
			this.setAttribute(AudioAttribute.ALBUM_PUBLICATION, dateText);
		}
		this.setAlbumIdentity(AlbumIdentity.create(track.getAlbum()));
		// artist
		this.setAttribute(AudioAttribute.ARTIST, track.getArtist().getName());
		this.setArtistDescription(track.getArtist().getDescription());
		// genre
		this.setAttribute(AudioAttribute.GENRE, track.getGenre().getName());
		// medium
		this.setAttribute(AudioAttribute.MEDIUM, track.getAlbum().getMedium().getName());
		// track
		this.setTrackId(track.getId());
		if (StringUtils.isNotBlank(track.getDiskName())) {
			this.setAttribute(AudioAttribute.DISK, track.getDiskName());
		}
		if (track.getRating() != null) {
			String value = AudioAttributeUtils.convert2String(track.getRating());
			this.setAttribute(AudioAttribute.RATING, value);
		}
		this.setTrack(track.getName());
		this.setTrackDescription(track.getDescription());
		if (StringUtils.isNotBlank(track.getFormat())) {
			this.setAttribute(AudioAttribute.TRACK_FORMAT, track.getFormat());
		}
		String trackNo = AudioAttributeUtils.convert2String(track.getNo());
		this.setAttribute(AudioAttribute.TRACK_NO, trackNo);
		setState(calculateState());	
	}
	
	public AudioData(Map<AudioAttribute, String> attributes) {
		this.attributes = new HashMap<>(attributes);
		this.support = new PropertyChangeSupport(this);
		setState(calculateState());
	}
	
	private AudioState calculateState() {
		boolean persisted = getTrackId() != null;
		//@formatter:off
		boolean complete = AudioDataHelper.isPresentAndNotDefaultValue(getMedium())
				&& AudioDataHelper.isPresentAndNotDefaultValue(getArtist())
				&& AudioDataHelper.isPresentAndNotDefaultValue(getAlbum())
				&& AudioDataHelper.isPresentAndNotDefaultValue(getTrack())
				&& AudioDataHelper.isPresentAndNotDefaultValue(getGenre());
		//@formatter:on
		if (persisted) {
			return complete ? AudioState.PERSISTENT : AudioState.PERSISTENT_INCOMPLETE;
		}
		else {
			return complete ? AudioState.TRANSIENT : AudioState.TRANSIENT_INCOMPLETE;
		}
	}

	/**
	 * Indicates if an attribute is set
	 * @param attribute
	 * @return boolean
	 */
	public boolean hasAttribute(AudioAttribute attribute) {
		return attributes.containsKey(attribute);
	}
	
	/**
	 * Removes an attribute
	 * @param attribute
	 */
	public void removeAttribute(AudioAttribute attribute) {
		attributes.remove(attribute);
	}
	
	/**
	 * Returns the value or null
	 * @param attribute
	 * @return
	 */
	public String getAttribute(AudioAttribute attribute) {
		return getAttribute(attribute, (String) null);
	}
	public String getAttribute(AudioAttribute attribute, String defaultValue) {
		if (hasAttribute(attribute)) {
			return attributes.get(attribute);
		}
		return defaultValue;
	}
	
	/**
	 * Returns the type-safe value or null
	 * @param attribute
	 * @return
	 * @see AudioAttribute#getType()
	 */
	public <T> T getAttribute(AudioAttribute attribute, Class<T> clazz) {
		if (hasAttribute(attribute)) {
			String value = getAttribute(attribute);
			if (attribute.getType().equals(clazz)) {
				Object obj = AudioAttributeUtils.getObjectValue(value, clazz);
				return clazz.cast(obj);
			}
			logger.warn("Illegal type of attribute: " + attribute);
		}
		return null;
	}

	/**
	 * Sets the value for an attribute
	 * @param attribute the attribute, must not be null
	 * @param value the value, must not be null
	 */
	public void setAttribute(AudioAttribute attribute, String value) {
		if (value != null) {
//			Object obj = AudioAttributeUtils.getObjectValue(value, attribute.getType());
//			if (obj == null) {
//				return;
//			}
//
//			if (hasAttribute(attribute)) {
//				if (getAttribute(attribute, attribute.getType()).equals(obj)) {
//					return;
//				}
//			}
			
			if (this.equalsAttribute(attribute, value)) {
				return;
			}
			
			support.firePropertyChange(attribute.name(), attributes.get(attribute), value);
			support.firePropertyChange(AUDIO_ATTRIBUTE_PROP, attributes.get(attribute), value);
			attributes.put(attribute, value);
		}
	}
	
	/**
	 * Indicates if a given value is equal to the attribute value of this object
	 * @param value
	 * @param attribute
	 * @return
	 */
	public boolean equalsAttribute(AudioAttribute attribute, String value) {
		if (value == null) {
			return false;
		}
		if (hasAttribute(attribute)) {
			Object compareValue = AudioAttributeUtils.getObjectValue(value, attribute.getType());
			Object currentValue = getAttribute(attribute, attribute.getType());
			if (currentValue != null && currentValue.equals(compareValue)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * @param state the state to set
	 */
	private void setState(AudioState state) {
		if (this.state == null || !this.state.equals(state)) {
			support.firePropertyChange(AUDIO_STATE_PROP, getState(), state);
			this.state = state;
		}
	}
	
	/**
	 * @return the track id
	 */
	public Long getTrackId() {
		return trackId;
	}

	/**
	 * @param trackId the track id to set
	 */
	private void setTrackId(Long trackId) {
		if (this.trackId == null || !this.trackId.equals(trackId)) {
			support.firePropertyChange(TRACK_ID_PROP, this.trackId, trackId);
			this.trackId = trackId;
		}
	}
	
	/**
	 * @param listener PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}
	
	/**
	 * @param listener PropertyChangeListener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}
	
	private void setMedium(String medium) {
		this.setAttribute(AudioAttribute.MEDIUM, medium);
	}
	public Optional<String> getMedium() {
		return Optional.ofNullable(getAttribute(AudioAttribute.MEDIUM));
	}

	private void setArtist(String artist) {
		this.setAttribute(AudioAttribute.ARTIST, artist);
	}
	public Optional<String> getArtist() {
		return Optional.ofNullable(getAttribute(AudioAttribute.ARTIST));
	}

	private void setAlbum(String album) {
		this.setAttribute(AudioAttribute.ALBUM, album);
	}
	public Optional<String> getAlbum() {
		return Optional.ofNullable(getAttribute(AudioAttribute.ALBUM));
	}

	private void setAlbumPublication(Date albumPublication) {
		String publicationString = AudioAttributeUtils.convert2String(albumPublication);
		this.setAttribute(AudioAttribute.ALBUM_PUBLICATION, publicationString);
	}
	public Optional<Date> getAlbumPublication() {
		String publicationString = getAttribute(AudioAttribute.ALBUM_PUBLICATION);
		if (StringUtils.isNotBlank(publicationString)) {
			return Optional.of(AudioAttributeUtils.convert2Date(publicationString));
		}
		return Optional.empty();
	}
	public void removeAlbumPublication() {
		this.removeAttribute(AudioAttribute.ALBUM_PUBLICATION);
	}

	private void setDisk(String disk) {
		this.setAttribute(AudioAttribute.DISK, disk);
	}
	public Optional<String> getDisk() {
		return Optional.ofNullable(getAttribute(AudioAttribute.DISK));
	}
	
	private void setTrack(String track) {
		this.setAttribute(AudioAttribute.TRACK, track);
	}
	public Optional<String> getTrack() {
		return Optional.ofNullable(getAttribute(AudioAttribute.TRACK));
	}

	private void setTrackNo(int trackNo) {
		this.setAttribute(AudioAttribute.TRACK_NO, String.valueOf(trackNo));
	}
	public Optional<Integer> getTrackNo() {
		return Optional.ofNullable(getAttribute(AudioAttribute.TRACK_NO, Integer.class));
	}

	private void setTrackFormat(String trackFormat) {
		this.setAttribute(AudioAttribute.TRACK_FORMAT, trackFormat);
	}
	public Optional<String> getTrackFormat() {
		return Optional.ofNullable(getAttribute(AudioAttribute.TRACK_FORMAT));
	}

	private void setGenre(String genre) {
		this.setAttribute(AudioAttribute.GENRE, genre);
	}
	public Optional<String> getGenre() {
		return Optional.ofNullable(getAttribute(AudioAttribute.GENRE));
	}

	private void setRating(RatingType rating) {
		this.setAttribute(AudioAttribute.RATING, rating.name());
	}
	public Optional<RatingType> getRating() {
		return Optional.ofNullable(getAttribute(AudioAttribute.RATING, RatingType.class));
	}
	
	public Optional<AlbumIdentity> getAlbumIdentifier() {
		return Optional.ofNullable(this.albumIdentity);
	}
	
	public Optional<String> getArtistDescription() {
		return Optional.ofNullable(this.artistDescription);
	}
	public Optional<String> getAlbumDescription() {
		return Optional.ofNullable(this.albumDescription);
	}
	public Optional<String> getTrackDescription() {
		return Optional.ofNullable(this.trackDescription);
	}
	
	@Override
	public AudioData clone() {
		return CloneUtils.deepCopy(this);
	}
	
	public void setAsModified() {
		AudioState newState = getState().nextForModified();
		setState(newState);
	}
	
	public void setAsPersisted(Long trackId) {
		setTrackId(trackId);
		AudioState newState = getState().nextForPersisted();
		setState(newState);
	}
	
	public void removeAll() {
		for (AudioAttribute attribute : AudioAttribute.values()) {
			this.removeAttribute(attribute);
		}
		this.setArtistDescription(null);
		this.setAlbumDescription(null);
		this.setAlbumIdentity(null);
		this.setTrackDescription(null);
		setState(AudioState.REMOVED);
	}
	
	public boolean applyChange(AudioDataChange change) {
		boolean modified = false;
		modified |= AudioDataHelper.applyChange(change.getMedium(), change.isMediumRemove(), this::setMedium, (v) -> this.removeAttribute(AudioAttribute.MEDIUM));
		modified |= AudioDataHelper.applyChange(change.getArtist(), change.isArtistRemove(), this::setArtist, (v) -> this.removeAttribute(AudioAttribute.ARTIST));
		modified |= AudioDataHelper.applyChange(change.getGenre(), change.isGenreRemove(), this::setGenre, (v) -> this.removeAttribute(AudioAttribute.GENRE));
		modified |= AudioDataHelper.applyChange(change.getAlbum(), change.isAlbumRemove(), this::setAlbum, (v) -> this.removeAttribute(AudioAttribute.ALBUM));
		modified |= AudioDataHelper.applyChange(change.getAlbumIdentity(), change.isAlbumIdentityRemove(), this::setAlbumIdentity, (v) -> this.setAlbumIdentity(null));
		modified |= AudioDataHelper.applyChange(change.getAlbumPublication(), change.isAlbumPublicationRemove(), this::setAlbumPublication, (v) -> this.removeAttribute(AudioAttribute.ALBUM_PUBLICATION));
		modified |= AudioDataHelper.applyChange(change.getDisk(), change.isDiskRemove(), this::setDisk, (v) -> this.removeAttribute(AudioAttribute.DISK));
		modified |= AudioDataHelper.applyChange(change.getTrack(), change.isTrackRemove(), this::setTrack, (v) -> this.removeAttribute(AudioAttribute.TRACK));
		modified |= AudioDataHelper.applyChange(change.getTrackNo(), change.isTrackNoRemove(), this::setTrackNo, (v) -> this.removeAttribute(AudioAttribute.TRACK_NO));
		modified |= AudioDataHelper.applyChange(change.getTrackFormat(), change.isTrackFormatRemove(), this::setTrackFormat, (v) -> this.removeAttribute(AudioAttribute.TRACK_FORMAT));
		modified |= AudioDataHelper.applyChange(change.getRating(), change.isRatingRemove(), this::setRating, (v) -> this.removeAttribute(AudioAttribute.RATING));
		
		if (modified) {
			updateRequiredAttributesAfterChange();
			AudioState newState = calculateState().nextForModified();
			setState(newState);
		}
		return modified;
	}

	private void updateRequiredAttributesAfterChange() {
		// set required attributes if empty
		AudioDataHelper.setDefaultValueIfEmpty(getMedium(), this::setMedium);
		AudioDataHelper.setDefaultValueIfEmpty(getArtist(), this::setArtist);
		AudioDataHelper.setDefaultValueIfEmpty(getAlbum(), this::setAlbum);
		AudioDataHelper.setDefaultValueIfEmpty(getTrack(), this::setTrack);
		AudioDataHelper.setDefaultValueIfEmpty(getGenre(), this::setGenre);
	}
	
	private static class AudioDataHelper {
		/**
		 * Apply change
		 * @return true if modified
		 */
		private static <V> boolean applyChange(@Nullable V value, boolean remove, Consumer<V> setConsumer, Consumer<V> removeConsumer) {
			if (value != null) {
				setConsumer.accept(value);
				return true;
			}
			else if (remove) {
				removeConsumer.accept(value);
				return true;
			}
			return false;
		}

		/**
		 * Sets the default value if no value is present
		 * @return true if modified
		 */
		private static <V> boolean setDefaultValueIfEmpty(Optional<String> valueOptional, Consumer<String> setConsumer) {
			if (valueOptional.isEmpty()) {
				setConsumer.accept(DEFAULT_VALUE);
				return true;
			}
			return false;
		}
		
		private static <V> boolean isPresentAndNotDefaultValue(Optional<String> valueOptional) {
			return valueOptional.isPresent() && !valueOptional.get().equals(DEFAULT_VALUE);
		}
	}
	
}
