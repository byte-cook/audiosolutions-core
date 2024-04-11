package de.kobich.audiosolutions.core.service.play;

import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.commons.ListenerList;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Represents the client. The audio device is a shared resource, only one client can be active at any one time.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AudioPlayerClient {
	@EqualsAndHashCode.Include
	private final String name;
	@Getter
	private final ListenerList<IAudioPlayerListener> listenerList;

	public AudioPlayerClient(String name) {
		this.name = name;
		this.listenerList = new ListenerList<IAudioPlayerListener>();
	}

}
