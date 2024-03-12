package de.kobich.audiosolutions.core.service.play.player;

import java.io.File;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.AudioPlayingThreadManager;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerState.UserAction;

/**
 * Base audio player.
 * @author ckorn
 */
@Service
public abstract class AbstractAudioPlayer {
	/**
	 * Plays the given file
	 * @param file
	 * @param request
	 * @return response
	 * @throws AudioException
	 */
	public abstract AudioPlayerResponse playFile(File file, long beginMillis, AudioPlayerClient client) throws AudioException;

	/**
	 * Checks user action and returns corresponding response. Should be called frequently while playing an audio file.
	 * @param state
	 * @param threadManager
	 * @return
	 * @throws AudioException
	 */
	protected Optional<AudioPlayerResponse> checkState(AudioPlayerState state, AudioPlayingThreadManager threadManager) throws AudioException {
		// check for interrupted
		if (this.isInterrupted(threadManager)) {
			state.requestStop();
		}

		switch (state.getUserAction()) {
		case PAUSE:
			state.resetUserAction();
			state.setPaused();
			doPause(state);
			return checkState(state, threadManager);
		case NONE:
			return Optional.empty();
		case RESUME:
			state.setPlaying(state.getCurrentFile(), state.getTotalMillis());
			state.resetUserAction();
			return Optional.empty();
		case SKIP:
			state.resetUserAction();
			long skipBeginMillis = state.getSkipBeginMillis();
			return Optional.of(new AudioPlayerResponse(PlayListFlowType.REPEAT_TRACK, skipBeginMillis));
		case STOP:
			state.resetUserAction();
			return Optional.of(new AudioPlayerResponse(PlayListFlowType.STOP));
		case NEXT:
			state.resetUserAction();
			return Optional.of(new AudioPlayerResponse(PlayListFlowType.NEXT_TRACK));
		case PREVIOUS:
			state.resetUserAction();
			return Optional.of(new AudioPlayerResponse(PlayListFlowType.PREVIOUS_TRACK));
		}
		return Optional.empty();
	}
	
	/**
	 * Indicates if thread should be interrupted
	 * @return
	 */
	private boolean isInterrupted(AudioPlayingThreadManager threadManager) {
		return Thread.currentThread().isInterrupted() || threadManager.isShutdown();
	}
	
	/**
	 * Makes a pause
	 */
	private void doPause(AudioPlayerState state) {
		while (UserAction.PAUSE.equals(state.getUserAction()) || UserAction.NONE.equals(state.getUserAction())) {
			try {
				state.resetUserAction();
				Thread.sleep(500);
			}
			catch (InterruptedException ex) {
				// Preserve interrupt status
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	/**
	 * Sleeps for a while
	 */
	protected void doSleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ex) {
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
