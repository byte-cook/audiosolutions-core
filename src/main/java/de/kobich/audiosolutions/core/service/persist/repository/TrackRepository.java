package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.persist.domain.Track;

public interface TrackRepository extends ListCrudRepository<Track, Long> { //, ListQueryByExampleExecutor<Track> {
	static interface TrackName {
		String getName();
	}
	
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.album.name = :albumName AND t.artist = :artist AND t.album.medium = :medium")
	Page<Track> findByArtistAndAlbumAndMedium(@Param("artist") Artist artist, @Param("albumName") String albumName, @Param("medium") Medium medium, Pageable page);
	
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.album.name = :albumName AND t.album.medium = :medium AND t.filePath LIKE :filePath ESCAPE :escape")
	@EntityGraph(value = Track.GRAPH)
	Page<Track> findByAlbumAndFilePath(@Param("albumName") String albumName, @Param("medium") Medium medium, @Param("filePath") String filePathWithWildcards, @Param("escape") char escape, Pageable page);

	//select t from Track t where t.filePath = :filePath
	@Transactional(readOnly = true)
	Optional<Track> findFirstByFilePath(String filePath);
	
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.artist.name IN (:artistNames)")
	List<Track> findByArtistNames(@Param("artistNames") Set<String> artistNames);
	
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.album.medium.name IN (:mediumNames)")
	List<Track> findByMediumNames(@Param("mediumNames") Set<String> mediumNames);
	
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.artist IN (:artists)")
	List<Track> findByArtists(@Param("artists") Set<Artist> artists);
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t.album IN (:albums)")
	List<Track> findByAlbums(@Param("albums") Set<Album> albums);
	@Transactional(readOnly = true)
	@Query("SELECT t FROM Track t WHERE t IN (:tracks)")
	List<Track> findByTracks(@Param("tracks") Set<Track> tracks);
	
	@Transactional(readOnly = true)
	<T> List<T> findAllByNameLikeIgnoreCaseOrderByName(String name, Class<T> type);
	
	@Transactional(readOnly = true)
	@Query("SELECT t.filePath FROM Track t WHERE t.filePath LIKE :filePath ESCAPE :escape")
	List<String> findFilePathLike(@Param("filePath") String filePathWithWildcards, @Param("escape") char escape);

	@Transactional(readOnly = true)
	@Query("SELECT count(DISTINCT t) FROM Track t INNER JOIN Album a ON t.album.id=a.id INNER JOIN Medium m ON a.medium.id=m.id WHERE a.medium IN (:mediums)")
	long countByMediums(@Param("mediums") Set<Medium> mediums);

}
