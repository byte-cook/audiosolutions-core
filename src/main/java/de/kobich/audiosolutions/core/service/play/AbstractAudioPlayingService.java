package de.kobich.audiosolutions.core.service.play;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.player.AbstractAudioPlayer;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerState;
import de.kobich.component.file.FileDescriptor;
import jakarta.annotation.PreDestroy;

/**
 * Base audio player.
 * @author ckorn
 */
@Service
public abstract class AbstractAudioPlayingService implements IAudioPlayingService {
	@Autowired
	private AudioPlayerState state;
	@Autowired
	private AudioPlayingThreadManager threadManager;
	@Autowired
	private AudioPlayerClientDispatcher dispatcher;

	/**
	 * Plays an audio file
	 * @param audioPlayer
	 * @param request
	 * @throws AudioException
	 */
	protected synchronized void play(AbstractAudioPlayer audioPlayer, AudioPlayerClient client, AudioPlayList playList) throws AudioException {
		stop(this.state.getClient());
		
		this.state.initState(client, playList);
		AudioPlayerRunnable runnable = new AudioPlayerRunnable(audioPlayer, state, dispatcher, client);
		threadManager.startRunnable(runnable);
	}
	
	@Override
	public synchronized void stop(AudioPlayerClient client) throws AudioException {
		checkClient(client);
		this.state.requestStop();
	}

	@Override
	public synchronized void pause(AudioPlayerClient client) throws AudioException {
		checkClient(client);
		this.state.requestPause();
	}

	@Override
	public synchronized void resume(AudioPlayerClient client) throws AudioException {
		checkClient(client);
		FileDescriptor file = state.getPlayList().getCurrentFile().orElse(null);
		if (file != null) {
			this.state.requestResume(file.getFile());
		}
	}
	
	@Override
	public synchronized void skip(AudioPlayerClient client, long beginMillis) throws AudioException {
		checkClient(client);
		this.state.requestSkip(beginMillis);
	}
	
	@Override
	public synchronized void next(AudioPlayerClient client) throws AudioException {
		checkClient(client);
		this.state.requestNext();
	}
	
	@Override
	public synchronized void previous(AudioPlayerClient client) throws AudioException {
		checkClient(client);
		this.state.requestPrevious();
	}
	
	protected void checkClient(AudioPlayerClient client) throws AudioException {
		if (this.state.getClient() != null && !this.state.getClient().equals(client)) {
			throw new AudioException(AudioException.ILLEGAL_STATE_ERROR);
		}
	}
	
	@PreDestroy
	public void stop() throws AudioException {
		stop(this.state.getClient());
	}
	
}
