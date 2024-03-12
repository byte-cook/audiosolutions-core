package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.persist.domain.Medium;

@Transactional(propagation = Propagation.MANDATORY)
public interface MediumRepository extends ListCrudRepository<Medium, Long> {
	static interface MediumName {
		String getName();
	}
	
	@Query("SELECT o FROM Medium o WHERE o.id IN (SELECT o.id FROM Medium o LEFT JOIN Album a ON o.id = a.medium.id WHERE a IS NULL)")
	Iterable<Medium> findAllWithoutTrack();
	
	@Modifying
//	@Query("DELETE FROM Medium o WHERE o.id NOT IN (SELECT t.album.medium.id FROM Track t)")
	@Query("DELETE FROM Medium o WHERE o.id IN (SELECT o.id FROM Medium o LEFT JOIN Album a ON o.id = a.medium.id WHERE a IS NULL)")
	void deleteAllWithoutTrack();

	Optional<Medium> findFirstByName(String name);
	
	List<Medium> findAllByNameLikeIgnoreCase(String name);
	
	<T> List<T> findAllByNameLikeIgnoreCaseOrderByName(String name, Class<T> type);
}
