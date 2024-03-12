package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;

@Transactional(propagation = Propagation.MANDATORY)
public interface ArtistRepository extends ListCrudRepository<Artist, Long> {
	static interface ArtistName {
		String getName();
	}
	
	@Transactional(readOnly = true)
	@Query("SELECT o FROM Artist o WHERE o.id IN (SELECT o.id FROM Artist o LEFT JOIN Track t ON o.id = t.artist.id WHERE t IS NULL)")
	Iterable<Artist> findAllWithoutTrack();
	
	@Modifying
//	@Query("DELETE FROM Artist o WHERE o.id NOT IN (SELECT t.album.artist.id FROM Track t)")
	@Query("DELETE FROM Artist o WHERE o.id IN (SELECT o.id FROM Artist o LEFT JOIN Track t ON o.id = t.artist.id WHERE t IS NULL)")
	void deleteAllWithoutTrack();
	
	@Transactional(readOnly = true)
	@Query("SELECT o FROM Artist o INNER JOIN Track t ON o.id = t.artist.id INNER JOIN Album a ON t.album.id = a.id WHERE a = :album")
	List<Artist> findAllByAlbum(@Param("album") Album album);
	
	Optional<Artist> findFirstByName(String name);

	List<Artist> findAllByNameLikeIgnoreCase(String name);

	<T> List<T> findAllByNameLikeIgnoreCaseOrderByName(String name, Class<T> type);

	@Transactional(readOnly = true)
	@Query("SELECT count(DISTINCT o) FROM Artist o INNER JOIN Track t ON o.id = t.artist.id INNER JOIN Album a ON t.album.id = a.id INNER JOIN Medium m ON a.medium.id=m.id WHERE a.medium IN (:mediums)")
	long countByMediums(@Param("mediums") Set<Medium> mediums);
}
