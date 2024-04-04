package de.kobich.audiosolutions.core.service.playlist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PlaylistRepository extends ListCrudRepository<Playlist, Long> {
	@Transactional(readOnly = true)
	Optional<Playlist> findByNameAndSystem(String name, boolean system);
	
	@Transactional(readOnly = true)
	List<Playlist> findAllByNameLikeIgnoreCaseAndSystem(String name, boolean system);
	
	@Transactional(readOnly = true)
	List<Playlist> findAllBySystem(boolean system);
	
	@EntityGraph(value = Playlist.GRAPH)
	Optional<Playlist> findEagerById(Long id);
}
