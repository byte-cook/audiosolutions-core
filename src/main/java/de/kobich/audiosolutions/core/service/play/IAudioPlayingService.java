package de.kobich.audiosolutions.core.service.play;

import de.kobich.audiosolutions.core.service.AudioException;

public interface IAudioPlayingService {
	public static final String JAVA_ZOOM_PLAYER = "javaZoom";
	public static final String MOCK_PLAYER = "mock";
	
	/**
	 * Plays an audio file
	 * @param request
	 * @throws AudioException
	 */
	void play(AudioPlayerClient client, IAudioPlayinglist playList) throws AudioException;

	/**
	 * Stops playing
	 * @param context
	 */
	void stop(AudioPlayerClient client) throws AudioException;

	/**
	 * Pauses playing
	 * @param context
	 */
	void pause(AudioPlayerClient client) throws AudioException;

	/**
	 * Resumes playing after pause
	 * @param context
	 */
	void resume(AudioPlayerClient client) throws AudioException;
	
	/**
	 * Rewinds/fast forwards playing to begin millis
	 * @param context
	 * @param beginMillis
	 */
	void rewind(AudioPlayerClient client, long beginMillis) throws AudioException;
	
	/**
	 * Jump to next track
	 * @param context
	 */
	void next(AudioPlayerClient client) throws AudioException;
	
	/**
	 * Jump to previous track
	 * @param context
	 */
	void previous(AudioPlayerClient client) throws AudioException;
	
}
