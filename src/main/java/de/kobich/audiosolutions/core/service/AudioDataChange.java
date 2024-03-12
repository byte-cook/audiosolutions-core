package de.kobich.audiosolutions.core.service;

import java.util.Date;

import de.kobich.component.file.FileDescriptor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class AudioDataChange {
	
	private final FileDescriptor fileDescriptor;

	private final String medium;
	private final boolean mediumRemove;

	private final String artist;
	private final boolean artistRemove;
	
	private final String genre;
	private final boolean genreRemove;
	
	private final String album;
	private final boolean albumRemove;
	
	private final AlbumIdentity albumIdentity;
	private final boolean albumIdentityRemove;
	
	private final Date albumPublication;
	private final boolean albumPublicationRemove;
	
	private final String disk;
	private final boolean diskRemove;
	
	private final String track;
	private final boolean trackRemove;
	
	private final Integer trackNo;
	private final boolean trackNoRemove;
	
	private final String trackFormat;
	private final boolean trackFormatRemove;
	
	private final RatingType rating;
	private final boolean ratingRemove;

	
}
