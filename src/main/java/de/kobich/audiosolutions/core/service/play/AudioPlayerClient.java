package de.kobich.audiosolutions.core.service.play;

import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.commons.ListenerList;

public class AudioPlayerClient {
	private final String name;
	private final ListenerList<IAudioPlayerListener> listenerList;

	public AudioPlayerClient(String name) {
		this.name = name;
		this.listenerList = new ListenerList<IAudioPlayerListener>();
	}

	public ListenerList<IAudioPlayerListener> getListenerList() {
		return listenerList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AudioPlayerClient other = (AudioPlayerClient) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
