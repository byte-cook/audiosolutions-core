package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.persist.domain.Genre;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;

//@Transactional(propagation = Propagation.MANDATORY)
public interface GenreRepository extends CrudRepository<Genre, Long> {
	static interface GenreName {
		String getName();
	}
	
	@Query("SELECT o FROM Genre o WHERE o.id IN (SELECT o.id FROM Genre o LEFT JOIN Track t ON o.id = t.genre.id WHERE t IS NULL)")
	Iterable<Genre> findAllWithoutTrack();
	
	@Modifying
//	@Query("DELETE FROM Genre o WHERE o.id NOT IN (SELECT t.genre.id FROM Track t)")
	@Query("DELETE FROM Genre o WHERE o.id IN (SELECT o.id FROM Genre o LEFT JOIN Track t ON o.id = t.genre.id WHERE t IS NULL)")
	void deleteAllWithoutTrack();

	Optional<Genre> findFirstByName(String name);
	
	<T> List<T> findAllByNameLikeIgnoreCaseOrderByName(String name, Class<T> type);

	@Transactional(readOnly = true)
	@Query("SELECT count(DISTINCT o) FROM Genre o INNER JOIN Track t ON o.id = t.genre.id INNER JOIN Album a ON t.album.id = a.id INNER JOIN Medium m ON a.medium.id=m.id WHERE a.medium IN (:mediums)")
	long countByMediums(@Param("mediums") Set<Medium> mediums);
}
