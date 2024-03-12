package de.kobich.audiosolutions.core.service;

import java.io.Serializable;
import java.util.UUID;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Defines an unique album. Files with the same {@link AlbumIdentity} are assigned to the same {@link Album} (if the medium is the same). 
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlbumIdentity implements Serializable {
	private static final long serialVersionUID = -3167139839608446180L;
	
	private final Long persistentId;
	private final UUID transientId;
	
	/**
	 * Returns {@link AlbumIdentity} for already persisted tracks in order to keep the current album assignment
	 * @param track
	 * @return
	 */
	public static AlbumIdentity create(Track track) {
		if (track.getAlbum() != null && track.getAlbum().getId() != null) {
			return new AlbumIdentity(track.getAlbum().getId(), null);
		}
		throw new IllegalStateException("Album is not persistent: " + track.getFilePathOnMedium());
	}
	
	/**
	 * Returns a new {@link AlbumIdentity}
	 */
	public static AlbumIdentity createNew() {
		return new AlbumIdentity(null, UUID.randomUUID());
	}
}