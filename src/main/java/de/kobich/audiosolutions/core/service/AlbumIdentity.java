package de.kobich.audiosolutions.core.service;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Defines an unique album. Files with the same {@link AlbumIdentity} are assigned to the same {@link Album} (if the medium is the same). 
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlbumIdentity implements Serializable {
	private static final long serialVersionUID = -3167139839608446180L;

	// persisted
	@EqualsAndHashCode.Include
	@Nullable
	private final Long persistentId;

	// transient
	@EqualsAndHashCode.Include
	@Nullable
	private final UUID transientId;
	
	/**
	 * Returns {@link AlbumIdentity} for already persisted tracks in order to keep the current album assignment
	 * @param album
	 * @return
	 */
	public static AlbumIdentity create(Album album) {
		if (album.getId() != null) {
			return new AlbumIdentity(album.getId(), null);
		}
		throw new IllegalStateException("Album is not persistent: " + album.getName());
	}
	
	/**
	 * Returns a new {@link AlbumIdentity}
	 */
	public static AlbumIdentity createNew() {
		return new AlbumIdentity(null, UUID.randomUUID());
	}

	public Optional<Long> getPersistentId() {
		return Optional.ofNullable(this.persistentId);
	}
}
