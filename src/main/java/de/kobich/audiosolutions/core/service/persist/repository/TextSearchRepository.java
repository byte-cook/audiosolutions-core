package de.kobich.audiosolutions.core.service.persist.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchToken;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchToken.SearchTokenType;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchTokens;
import de.kobich.commons.utils.SQLUtils;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
@Transactional(rollbackFor=AudioException.class, readOnly = true)
public class TextSearchRepository {
	private static final Logger logger = Logger.getLogger(TextSearchRepository.class);
	@PersistenceContext
	private EntityManager entityManager;
	
	public List<Artist> findArtistsByTokens(AudioTextSearchTokens tokens, int maxResults) {
		Map<String, String> parameters = new HashMap<>();
		StringBuilder queryString;
		if (tokens.isAllUndefined() || tokens.containsOnlyTypes(SearchTokenType.ARTIST, SearchTokenType.ARTIST_NOT)) {
			queryString = new StringBuilder("SELECT ar FROM Artist ar");
			queryString.append(createSimpleWhereClause(tokens, parameters, "ar.name"));
		}
		else {
			queryString = new StringBuilder("SELECT DISTINCT ar FROM Artist ar INNER JOIN Track t ON ar.id = t.artist.id INNER JOIN Album a ON t.album.id = a.id");
			queryString.append(createWhereClause(tokens, SearchTokenType.ARTIST, parameters, "ar.name", "a.name", "t.name", "a.medium.name", "t.genre.name"));
		}
		
		logger.info("Query to find artists: " + queryString.toString());
		TypedQuery<Artist> sqlQuery = entityManager.createQuery(queryString.toString(), Artist.class);
		sqlQuery.setMaxResults(maxResults);
		for (Entry<String, String> entry : parameters.entrySet()) {
			sqlQuery.setParameter(entry.getKey(), entry.getValue());
		}
		List<Artist> artists = sqlQuery.getResultList().stream().limit(maxResults).toList();
		logger.debug("Found " + artists.size() + " artists for query: " + queryString.toString());
		return artists;
	}
	
	public List<Album> findAlbumsByTokens(AudioTextSearchTokens tokens, int maxResults) {
		Map<String, String> parameters = new HashMap<>();
		StringBuilder queryString;
		if (tokens.isAllUndefined() || tokens.containsOnlyTypes(SearchTokenType.ALBUM, SearchTokenType.ALBUM_NOT)) {
			queryString = new StringBuilder("SELECT a FROM Album a");
			queryString.append(createSimpleWhereClause(tokens, parameters, "a.name"));
		}
		else {
			queryString = new StringBuilder("SELECT DISTINCT a FROM Album a INNER JOIN Track t ON a.id = t.album.id INNER JOIN Artist ar ON t.artist.id = ar.id");
			queryString.append(createWhereClause(tokens, SearchTokenType.ALBUM, parameters, "ar.name", "a.name", "t.name", "a.medium.name", "t.genre.name"));
		}
		
		logger.info("Query to find albums: " + queryString.toString());
		TypedQuery<Album> sqlQuery = entityManager.createQuery(queryString.toString(), Album.class);
		sqlQuery.setMaxResults(maxResults);
		EntityGraph<?> graph = entityManager.createEntityGraph(Album.GRAPH);
		sqlQuery.setHint(AvailableHints.HINT_SPEC_FETCH_GRAPH, graph);
		for (Entry<String, String> entry : parameters.entrySet()) {
			sqlQuery.setParameter(entry.getKey(), entry.getValue());
		}
		List<Album> albums = sqlQuery.getResultList().stream().limit(maxResults).toList();
		logger.debug("Found " + albums.size() + " albums for query: " + queryString.toString());
		return albums;
	}

	public List<Track> findTracksByTokens(AudioTextSearchTokens tokens, int maxResults) {
		// DISTINCT not necessary here
		StringBuilder queryString = new StringBuilder("SELECT t FROM Track t INNER JOIN Album a ON t.album.id = a.id INNER JOIN Artist ar ON t.artist.id = ar.id");
		Map<String, String> parameters = new HashMap<>();
		queryString.append(createWhereClause(tokens, SearchTokenType.TRACK, parameters, "ar.name", "a.name", "t.name", "a.medium.name", "t.genre.name"));
		
		logger.info("Query to find tracks: " + queryString.toString());
		TypedQuery<Track> sqlQuery = entityManager.createQuery(queryString.toString(), Track.class);
		sqlQuery.setMaxResults(maxResults);
		EntityGraph<?> graph = entityManager.createEntityGraph(Track.GRAPH);
		sqlQuery.setHint(AvailableHints.HINT_SPEC_FETCH_GRAPH, graph);
		for (Entry<String, String> entry : parameters.entrySet()) {
			sqlQuery.setParameter(entry.getKey(), entry.getValue());
		}
		List<Track> tracks = sqlQuery.getResultList();
		logger.debug("Found " + tracks.size() + " tracks for query: " + queryString.toString());
		return tracks;
	}
	
	private StringBuilder createSimpleWhereClause(AudioTextSearchTokens tokens, Map<String, String> parameters, String jpql) {
		final String condition = " lower("+jpql+") LIKE lower(:%s)";
		final String notCondition = " lower("+jpql+") NOT LIKE lower(:%s)";
		StringBuilder whereClause = new StringBuilder();
		
		String combineString = " WHERE";
		for (AudioTextSearchToken token : tokens.getTokens()) {
			final String paramName = "p" + parameters.size();
		
			whereClause.append(combineString);
			whereClause.append(String.format(token.getType().isNegate() ? notCondition : condition, paramName));
			parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
			
			combineString = " and";
		}
		return whereClause;
	}
	
	private StringBuilder createWhereClause(AudioTextSearchTokens tokens, SearchTokenType defaultType, Map<String, String> parameters, String artistNameJPQL, String albumNameJPQL, String trackNameJPQL, String mediumNameJPQL, String genreNameJPQL) {
		final String artistCondition = " lower("+artistNameJPQL+") LIKE lower(:%s)";
		final String artistNotCondition = " lower("+artistNameJPQL+") NOT LIKE lower(:%s)";
		final String albumCondition = " lower("+albumNameJPQL+") LIKE lower(:%s)";
		final String albumNotCondition = " lower("+albumNameJPQL+") NOT LIKE lower(:%s)";
		final String trackCondition = " lower("+trackNameJPQL+") LIKE lower(:%s)";
		final String trackNotCondition = " lower("+trackNameJPQL+") NOT LIKE lower(:%s)";
		final String mediumCondition = " lower("+mediumNameJPQL+") LIKE lower(:%s)";
		final String mediumNotCondition = " lower("+mediumNameJPQL+") NOT LIKE lower(:%s)";
		final String genreCondition = " lower("+genreNameJPQL+") LIKE lower(:%s)";
		final String genreNotCondition = " lower("+genreNameJPQL+") NOT LIKE lower(:%s)";
		
		StringBuilder whereClause = new StringBuilder();
		
		String combineString = " WHERE";
		for (AudioTextSearchToken token : tokens.getTokens()) {
			final String paramName = "p" + parameters.size();
			whereClause.append(combineString);
			
			SearchTokenType type = token.getType();
			if (type.isUndefined() && tokens.isAllUndefined()) {
				// improve performance by making WHERE clause simpler:
				// change undefined tokens only if all tokens are undefined
				// e.g.: artist:stones "start me up" 
				type = defaultType;
			}
			switch (type) {
				case ARTIST:
				case ARTIST_NOT:
					whereClause.append(String.format(token.getType().isNegate() ? artistNotCondition : artistCondition, paramName));
					parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
				case ALBUM:
				case ALBUM_NOT:
					whereClause.append(String.format(token.getType().isNegate() ? albumNotCondition : albumCondition, paramName));
					parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
				case TRACK:
				case TRACK_NOT:
					whereClause.append(String.format(token.getType().isNegate() ? trackNotCondition : trackCondition, paramName));
					parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
				case MEDIUM:
				case MEDIUM_NOT:
					whereClause.append(String.format(token.getType().isNegate() ? mediumNotCondition : mediumCondition, paramName));
					parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
				case GENRE:
				case GENRE_NOT:
					whereClause.append(String.format(token.getType().isNegate() ? genreNotCondition : genreCondition, paramName));
					parameters.put(paramName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
				case UNDEFINED:
					// if not specified: search in artist OR album OR track
					final String artistName = "artist" + parameters.size();
					final String albumName = "album" + parameters.size();
					final String trackName = "track" + parameters.size();
					whereClause.append(String.format("(" + artistCondition + " OR " + albumCondition + " OR " + trackCondition + ")", artistName, albumName, trackName));
					parameters.put(artistName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					parameters.put(albumName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					parameters.put(trackName, SQLUtils.escapeLikeParam(token.getValue(), true, true));
					break;
			}
			combineString = " and";
		}
		return whereClause;
	}

}
