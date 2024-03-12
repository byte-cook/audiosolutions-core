package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;

@Transactional(propagation = Propagation.MANDATORY)
public interface AlbumRepository extends CrudRepository<Album, Long> {
	static interface AlbumName {
		String getName();
	}
	
//	@Query("SELECT o FROM Album o LEFT JOIN Track t WHERE t IS NULL")
	@Query("SELECT o FROM Album o WHERE o.id IN (SELECT o.id FROM Album o LEFT JOIN Track t ON o.id = t.album.id WHERE t IS NULL)")
	Iterable<Album> findAllWithoutTrack();
	
	@Modifying
	@Query("DELETE FROM Album o WHERE o.id IN (SELECT o.id FROM Album o LEFT JOIN Track t ON o.id = t.album.id WHERE t IS NULL)")
	void deleteAllWithoutTrack();

//	Optional<Album> findFirstByNameAndMedium(String name, Medium medium);
	List<Album> findAllByNameAndMedium(String name, Medium medium);
	
	<T> List<T> findAllByNameLikeIgnoreCaseOrderByName(String name, Class<T> type);
	
	@Transactional(readOnly = true)
	@Query("SELECT count(DISTINCT o) FROM Album o INNER JOIN Medium m ON o.medium.id=m.id WHERE o.medium IN (:mediums)")
	long countByMediums(@Param("mediums") Set<Medium> mediums);
}
