package de.kobich.audiosolutions.core.service.play.player;

import java.io.File;

import de.kobich.audiosolutions.core.service.AudioException;

public interface IAudioPlayerListener {
	static final long TOTAL_MILLIS_UNDEFINED = -1;
	
	/**
	 * Indicates if playing is started
	 */
	void play(final File file, final long totalMillis);
	
	/**
	 * Indicates if playing is resumed
	 */
	void resume();
	
	/**
	 * Indicates if playing is stopped
	 */
	void stopped();

	/**
	 * Indicates if playing is paused
	 */
	void paused();

	/**
	 * Indicates current played millis
	 */
	void playedMillis(final long millis);
	
	/**
	 * Indicates an error
	 * @param exc
	 */
	void errorOccured(final AudioException exc);
	
}
