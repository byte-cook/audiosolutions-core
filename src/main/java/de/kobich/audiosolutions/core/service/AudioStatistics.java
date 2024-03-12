package de.kobich.audiosolutions.core.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioStatistics {
	private final long mediumCount;
	private final long genreCount;
	private final long artistCount;
	private final long albumCount;
	private final long trackCount;
}
