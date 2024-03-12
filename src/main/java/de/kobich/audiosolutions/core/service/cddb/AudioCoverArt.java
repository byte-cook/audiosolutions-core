package de.kobich.audiosolutions.core.service.cddb;

import java.io.InputStream;
import java.util.Optional;

public class AudioCoverArt {
	private InputStream front;
	private InputStream back;
	
	public AudioCoverArt(InputStream front, InputStream back) {
		this.front = front;
		this.back = back;
	}
	
	public Optional<InputStream> getFront() {
		return Optional.ofNullable(front);
	}

	public Optional<InputStream> getBack() {
		return Optional.ofNullable(back);
	}
}
