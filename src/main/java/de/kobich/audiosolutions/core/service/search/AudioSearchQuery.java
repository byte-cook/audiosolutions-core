package de.kobich.audiosolutions.core.service.search;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class AudioSearchQuery {
	@Nullable
	private final String mediumName;
	@Nullable
	private final Long mediumId;
	
	@Nullable
	private final String genreName;
	@Nullable
	private final Long genreId;

	@Nullable
	private final String artistName;
	@Nullable
	private final Long artistId;
	
	@Nullable
	private final String albumName;
	@Nullable
	private final Long albumId;
	
	@Nullable
	private final String trackName;
	@Nullable
	private final String trackFormat;
	@Nullable
	private final Long trackId;

}
