package de.kobich.audiosolutions.core.service.play;

import java.io.File;
import java.util.Optional;

import org.apache.log4j.Logger;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.player.AbstractAudioPlayer;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerState;

/**
 * The playing thread. Only one thread can be active at any one time.
 */
public class AudioPlayerRunnable implements Runnable {
	private static final Logger logger = Logger.getLogger(AudioPlayerRunnable.class);
	private final AbstractAudioPlayer audioPlayer;
	private final AudioPlayerState state;
	private final AudioPlayerClientDispatcher dispatcher;
	private final AudioPlayerClient client;
	
	public AudioPlayerRunnable(AbstractAudioPlayer audioPlayer, AudioPlayerState state, AudioPlayerClientDispatcher dispatcher, AudioPlayerClient client) {
		this.audioPlayer = audioPlayer;
		this.state = state;
		this.dispatcher = dispatcher;
		this.client = client;
	}

	@Override
	public void run() {
		try {
			logger.debug("Start playing...");
			try {
				long beginMillis = 0;
				IAudioPlayinglist playlist = state.getPlayList();
				
				Optional<File> fileOpt = playlist.getStartFile();
				while (fileOpt.isPresent()) {
					File file = fileOpt.get();
					if (!file.exists() || file.length() == 0) {
						fileOpt = playlist.getNextFile();
						continue;
					}
					
					// play now
					AudioPlayerResponse response = audioPlayer.playFile(file, beginMillis, client);
					beginMillis = response.getNextBeginMillis();
					PlayListFlowType type = response.getFlowType();
					switch (type) {
						case TRACK_FINISHED:
						case NEXT_TRACK:
							fileOpt = playlist.getNextFile();
							break;
						case PREVIOUS_TRACK:
							fileOpt = playlist.getPreviousFile();
							break;
						case STOP:
							fileOpt = Optional.empty();
							break;
						case REPEAT_TRACK:
							break;
					}
				}
			}
			catch (AudioException exc) {
				this.dispatcher.fireErrorOccured(client, exc);
				throw exc;
			}
			catch (Exception exc) {
				throw new AudioException(AudioException.INTERNAL, exc);
			}
			finally {
				state.setStopped();
			}
		}
		catch (AudioException exc) {
			logger.error("Error while playing", exc);
		}
	}

}
