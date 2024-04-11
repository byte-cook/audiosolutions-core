package de.kobich.audiosolutions.core.service.play.player;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClientDispatcher;
import de.kobich.audiosolutions.core.service.play.IAudioPlayinglist;
import jakarta.annotation.PostConstruct;

/**
 * Holds the current player state.
 * Provides setXXX() methods to change the state and requestXXX() methods to request a state change.
 * 
 * @author ckorn
 */
@Service
public class AudioPlayerState {
	private static final Logger logger = Logger.getLogger(AudioPlayerState.class);

	public static enum PlayerState {
		PLAYING, PAUSED, STOPPED
	}

	public static enum UserAction {
		NONE, PAUSE, RESUME, STOP, REWIND, NEXT, PREVIOUS
	}

	@Autowired
	private AudioPlayerClientDispatcher dispatcher;

	private AudioPlayerClient client;
	private IAudioPlayinglist playList;
	private PlayerState state;
	private UserAction userAction;
	private long rewindBeginMillis;
	private File currentFile;
	private long totalMillis;
	/**
	 * Lock for state changes
	 */
	private final Object STATE_MONITOR = new Object();
	/**
	 * Lock for user actions: Playing and stopping must take place one after the other
	 */
	private final Object USER_ACTION_MONITOR = new Object();
	/**
	 * Lock for reset action: as soon as a {@link UserAction} is completed, it is reset
	 */
	private final Object RESET_ACTION_MONITOR = new Object();

	@PostConstruct
	public void postConstruct() {
		this.state = PlayerState.STOPPED;
		this.userAction = UserAction.NONE;
	}

	/**
	 * Initialize this state
	 * @param audioPlayerClient
	 * @param playList
	 */
	public void initState(AudioPlayerClient audioPlayerClient, IAudioPlayinglist playList) {
		synchronized (STATE_MONITOR) {
			this.client = audioPlayerClient;
			this.playList = playList;
			this.userAction = UserAction.NONE;
			this.state = PlayerState.STOPPED;
		}
	}

	/**
	 * Sets state to PLAYING
	 */
	public void setPlaying(File file, long totalMillis) {
		synchronized (STATE_MONITOR) {
			this.state = PlayerState.PLAYING;
			this.currentFile = file;
			this.totalMillis = totalMillis;
			this.dispatcher.firePlay(client, file, totalMillis);
		}
	}

	/**
	 * Sets state to PAUSED
	 */
	public void setPaused() {
		synchronized (STATE_MONITOR) {
			this.state = PlayerState.PAUSED;
			this.dispatcher.firePaused(client);
		}
	}

	/**
	 * Sets state to STOPPED
	 */
	public void setStopped() {
		synchronized (STATE_MONITOR) {
			this.state = PlayerState.STOPPED;
			this.dispatcher.fireStopped(client);
		}
	}

	public void requestPause() throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			if (PlayerState.PAUSED.equals(state)) {
				return;
			}
			checkState(PlayerState.PLAYING, PlayerState.PAUSED);
			this.userAction = UserAction.PAUSE;
			awaitResetAction();
		}
	}

	public void requestResume() throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			checkState(PlayerState.PAUSED);
			this.userAction = UserAction.RESUME;
			awaitResetAction();
			dispatcher.fireResume(client);
		}
	}

	public void requestStop() throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			if (PlayerState.STOPPED.equals(state)) {
				return;
			}
			this.userAction = UserAction.STOP;
			awaitResetAction();
		}
	}

	public long getRewindBeginMillis() {
		return rewindBeginMillis;
	}

	public void requestRewind(long beginMillis) throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			checkState(PlayerState.PLAYING, PlayerState.PAUSED);
			this.rewindBeginMillis = beginMillis;
			this.userAction = UserAction.REWIND;
			awaitResetAction();
		}
	}

	public void requestNext() throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			checkState(PlayerState.PLAYING, PlayerState.PAUSED);
			this.userAction = UserAction.NEXT;
			awaitResetAction();
		}
	}

	public void requestPrevious() throws AudioException {
		synchronized (USER_ACTION_MONITOR) {
			checkState(PlayerState.PLAYING, PlayerState.PAUSED);
			this.userAction = UserAction.PREVIOUS;
			awaitResetAction();
		}
	}

	public void resetUserAction() {
		if (UserAction.NONE.equals(this.userAction)) {
			return;
		}
		logger.debug("RESET: begin");
		synchronized (RESET_ACTION_MONITOR) {
			logger.debug("RESET: in lock");
			this.userAction = UserAction.NONE;
			RESET_ACTION_MONITOR.notifyAll();
		}
		logger.debug("RESET: end");
	}

	private void awaitResetAction() {
		logger.debug("AWAIT: begin");
		while (!UserAction.NONE.equals(this.userAction) && !PlayerState.STOPPED.equals(this.state)) {
			try {
				logger.debug("AWAIT: userAction=" + this.userAction);
				synchronized (RESET_ACTION_MONITOR) {
					logger.debug("AWAIT: in lock");
					RESET_ACTION_MONITOR.wait(TimeUnit.MILLISECONDS.toMillis(100));
				}
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
			}
		}
		logger.debug("AWAIT: end");
	}

	private void checkState(PlayerState... allowedStates) throws AudioException {
		for (PlayerState s : allowedStates) {
			if (this.state.equals(s)) {
				return;
			}
		}
		throw new AudioException(AudioException.ILLEGAL_STATE_ERROR);
	}

	public PlayerState getPlayerState() {
		return this.state;
	}

	public UserAction getUserAction() {
		return this.userAction;
	}

	public AudioPlayerClient getClient() {
		return this.client;
	}

	public IAudioPlayinglist getPlayList() {
		return this.playList;
	}

	public File getCurrentFile() {
		return this.currentFile;
	}

	public long getTotalMillis() {
		return this.totalMillis;
	}

}
