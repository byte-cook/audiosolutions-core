package de.kobich.audiosolutions.core.service.search;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.persist.repository.TextSearchRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(rollbackFor=AudioException.class, readOnly = true)
@RequiredArgsConstructor
public class AudioTextSearchService {
	private static final Logger logger = Logger.getLogger(AudioSearchService.class);
	@Autowired
	private final AudioTextSearchTokenizerService tokenizerService;
	@Autowired
	private final TextSearchRepository searchRepository;

	public AudioTextSearchResult search(String input, int maxResults) throws AudioException {
		final AudioTextSearchTokens tokens = tokenizerService.tokenize(input);
		logger.info(String.format("Input: <%s> -> Tokens: %s", input, tokens));
		
		StopWatch watch = new StopWatch();
		watch.start();
		List<Artist> artists = searchRepository.findArtistsByTokens(tokens, maxResults);
		logger.info(String.format("Find artists takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		watch.reset();
		
		watch.start();
		List<Album> albums = searchRepository.findAlbumsByTokens(tokens, maxResults);
		logger.info(String.format("Find albums takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		watch.reset();

		watch.start();
		List<Track> tracks = searchRepository.findTracksByTokens(tokens, maxResults);
		logger.info(String.format("Find tracks takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		return new AudioTextSearchResult(artists, albums, tracks);
	}
	
	public AudioTextSearchResult searchSimultaneously(String input, int maxResults) throws AudioException {
		final AudioTextSearchTokens tokens = tokenizerService.tokenize(input);
		logger.info(String.format("Input: <%s> -> Tokens: %s", input, tokens));
		StopWatch completeWatch = new StopWatch();
		completeWatch.start();
		
		CompletableFuture<List<Artist>> artists = CompletableFuture.supplyAsync(() -> {
			StopWatch watch = new StopWatch();
			watch.start();
			try {
				return searchRepository.findArtistsByTokens(tokens, maxResults);
			} finally {
				logger.info(String.format("Find artists takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
			}
		});
		CompletableFuture<List<Album>> albums = CompletableFuture.supplyAsync(() -> {
			StopWatch watch = new StopWatch();
			watch.start();
			try {
				return searchRepository.findAlbumsByTokens(tokens, maxResults);
			} finally {
				logger.info(String.format("Find albums takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
			}
		});
		CompletableFuture<List<Track>> tracks = CompletableFuture.supplyAsync(() -> {
			StopWatch watch = new StopWatch();
			watch.start();
			try {
				return searchRepository.findTracksByTokens(tokens, maxResults);
			} finally {
				logger.info(String.format("Find tracks takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
			}
		});
		
		try {
			return new AudioTextSearchResult(artists.get(), albums.get(), tracks.get());
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AudioException(AudioException.INTERNAL);
		}
		finally {
			logger.info(String.format("Find all takes %dms", completeWatch.getTime(TimeUnit.MILLISECONDS)));
		}
		
	}

	public List<Artist> searchArtists(String input, int maxResults) throws AudioException {
		final AudioTextSearchTokens tokens = tokenizerService.tokenize(input);
		logger.info(String.format("Input: <%s> -> Tokens: %s", input, tokens));

		StopWatch watch = new StopWatch();
		watch.start();
		List<Artist> artists = searchRepository.findArtistsByTokens(tokens, maxResults);
		logger.info(String.format("Find artists takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		return artists;
	}

	public List<Album> searchAlbums(String input, int maxResults) throws AudioException {
		final AudioTextSearchTokens tokens = tokenizerService.tokenize(input);
		logger.info(String.format("Input: <%s> -> Tokens: %s", input, tokens));

		StopWatch watch = new StopWatch();
		watch.start();
		List<Album> albums = searchRepository.findAlbumsByTokens(tokens, maxResults);
		logger.info(String.format("Find albums takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		return albums;
	}

	public List<Track> searchTracks(String input, int maxResults) throws AudioException {
		final AudioTextSearchTokens tokens = tokenizerService.tokenize(input);
		logger.info(String.format("Input: <%s> -> Tokens: %s", input, tokens));

		StopWatch watch = new StopWatch();
		watch.start();
		List<Track> tracks = searchRepository.findTracksByTokens(tokens, maxResults);
		logger.info(String.format("Find tracks takes %dms", watch.getTime(TimeUnit.MILLISECONDS)));
		return tracks;
	}

}
