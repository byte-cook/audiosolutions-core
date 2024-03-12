package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.commons.utils.SQLUtils;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class TrackSearchRepository {
	private static final Logger logger = Logger.getLogger(TrackSearchRepository.class);
	@PersistenceContext
	private EntityManager entityManager;

	public List<Track> findByQuery(AudioSearchQuery query) {
		StringBuilder queryString = new StringBuilder("SELECT t FROM Track t");
		String combineString = " where ";
		if (StringUtils.hasText(query.getMediumName())) {
			queryString.append(combineString + "lower(t.album.medium.name) like lower(:mediumName)");
			combineString = " and ";
		}
		if (query.getMediumId() != null) {
			queryString.append(combineString + "t.album.medium.id = :mediumId");
			combineString = " and ";
		}
		if (StringUtils.hasText(query.getArtistName())) {
			queryString.append(combineString + "lower(t.artist.name) like lower(:artistName)");
			combineString = " and ";
		}
		if (query.getArtistId() != null) {
			queryString.append(combineString + "t.artist.id = :artistId");
			combineString = " and ";
		}
		if (StringUtils.hasText(query.getGenreName())) {
			queryString.append(combineString + "lower(t.genre.name) like lower(:genreName)");
			combineString = " and ";
		}
		if (query.getGenreId() != null) {
			queryString.append(combineString + "t.genre.id = :genreId");
			combineString = " and ";
		}
		if (StringUtils.hasText(query.getAlbumName())) {
			queryString.append(combineString + "lower(t.album.name) like lower(:albumName)");
			combineString = " and ";
		}
		if (query.getAlbumId() != null) {
			queryString.append(combineString + "t.album.id = :albumId");
			combineString = " and ";
		}
		if (StringUtils.hasText(query.getTrackName())) {
			queryString.append(combineString + "lower(t.name) like lower(:trackName)");
			combineString = " and ";
		}
		if (StringUtils.hasText(query.getTrackFormat())) {
			queryString.append(combineString + "lower(t.format) like lower(:trackFormat)");
			combineString = " and ";
		}
		if (query.getTrackId() != null) {
			queryString.append(combineString + "t.id = :trackId");
			combineString = " and ";
		}
		
		TypedQuery<Track> sqlQuery = entityManager.createQuery(queryString.toString(), Track.class);
		EntityGraph<?> graph = entityManager.createEntityGraph(Track.GRAPH);
		sqlQuery.setHint(AvailableHints.HINT_SPEC_FETCH_GRAPH, graph);
		
		if (StringUtils.hasText(query.getMediumName())) {
			sqlQuery.setParameter("mediumName", SQLUtils.escapeLikeParam(query.getMediumName(), true, true));
		}
		if (query.getMediumId() != null) {
			sqlQuery.setParameter("mediumId", query.getMediumId());
		}
		if (StringUtils.hasText(query.getArtistName())) {
			sqlQuery.setParameter("artistName", SQLUtils.escapeLikeParam(query.getArtistName(), true, true));
		}
		if (query.getArtistId() != null) {
			sqlQuery.setParameter("artistId", query.getArtistId());
		}
		if (StringUtils.hasText(query.getGenreName())) {
			sqlQuery.setParameter("genreName", SQLUtils.escapeLikeParam(query.getGenreName(), true, true));
		}
		if (query.getGenreId() != null) {
			sqlQuery.setParameter("genreId", query.getGenreId());
		}
		if (StringUtils.hasText(query.getAlbumName())) {
			sqlQuery.setParameter("albumName", SQLUtils.escapeLikeParam(query.getAlbumName(), true, true));
		}
		if (query.getAlbumId() != null) {
			sqlQuery.setParameter("albumId", query.getAlbumId());
		}
		if (StringUtils.hasText(query.getTrackName())) {
			sqlQuery.setParameter("trackName", SQLUtils.escapeLikeParam(query.getTrackName(), true, true));
		}
		if (StringUtils.hasText(query.getTrackFormat())) {
			sqlQuery.setParameter("trackFormat", SQLUtils.escapeLikeParam(query.getTrackFormat(), true, true));
		}
		if (query.getTrackId() != null) {
			sqlQuery.setParameter("trackId", query.getTrackId());
		}
//		query.setMaxResults(AudioSolutions.getInstance().getMaxResults());

		List<Track> tracks = sqlQuery.getResultList();
		logger.debug("Found " + tracks.size() + " tracks for query: " + queryString.toString());
		return tracks;
	}

}
