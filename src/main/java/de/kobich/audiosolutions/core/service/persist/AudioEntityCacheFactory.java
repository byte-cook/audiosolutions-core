package de.kobich.audiosolutions.core.service.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository;
import de.kobich.audiosolutions.core.service.persist.repository.GenreRepository;
import de.kobich.audiosolutions.core.service.persist.repository.MediumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository;

@Service
public class AudioEntityCacheFactory {
	@Autowired
	private ArtistRepository artistRepository;
	@Autowired
	private AlbumRepository albumRepository;
	@Autowired
	private GenreRepository genreRepository;
	@Autowired
	private MediumRepository mediumRepository;
	@Autowired
	private TrackRepository trackRepository;

	public AudioEntityCache createCache() {
		return new AudioEntityCache(mediumRepository, artistRepository, genreRepository, albumRepository, trackRepository);
	}
}
