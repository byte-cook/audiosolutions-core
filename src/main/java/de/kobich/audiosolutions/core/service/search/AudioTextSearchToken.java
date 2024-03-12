package de.kobich.audiosolutions.core.service.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AudioTextSearchToken {
	@RequiredArgsConstructor
	@Getter
	public enum SearchTokenType {
		//@formatter:off
		UNDEFINED("", false), 
		MEDIUM("medium", false), MEDIUM_NOT("-medium", true), 
		GENRE("genre", false), GENRE_NOT("-genre", true), 
		ARTIST("artist", false), ARTIST_NOT("-artist", true), 
		ALBUM("album", false), ALBUM_NOT("-album", true), 
		TRACK("track", false), TRACK_NOT("-track", true);
		//@formatter:on

		private final String keyWord;
		private final boolean negate;

		public boolean isUndefined() {
			return UNDEFINED.equals(this);
		}
	}

	private final SearchTokenType type;
	private final String value;

}
