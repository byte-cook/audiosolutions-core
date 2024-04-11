package de.kobich.audiosolutions.core.service.play;

import java.io.File;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;

/**
 * Notifies audio player client listeners.
 * 
 * @author ckorn
 */
@Service
public class AudioPlayerClientDispatcher {

	public synchronized void firePlay(AudioPlayerClient client, File file, long totalMillis) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.play(file, totalMillis);
		}
	}

	public synchronized void fireResume(AudioPlayerClient client) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.resume();
		}
	}

	public synchronized void firePaused(AudioPlayerClient client) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.paused();
		}
	}

	public synchronized void fireStopped(AudioPlayerClient client) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.stopped();
		}
	}

	public synchronized void firePlayedMillis(AudioPlayerClient client, final long playedMillis) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.playedMillis(playedMillis);
		}
	}

	public synchronized void fireErrorOccured(AudioPlayerClient client, AudioException exc) {
		for (IAudioPlayerListener l : client.getListenerList()) {
			l.errorOccured(exc);
		}
	}

}
