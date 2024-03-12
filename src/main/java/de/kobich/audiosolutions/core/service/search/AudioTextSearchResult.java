package de.kobich.audiosolutions.core.service.search;

import java.util.List;

import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AudioTextSearchResult {
	private final List<Artist> artists;
	private final List<Album> albums;
	private final List<Track> tracks;

}
