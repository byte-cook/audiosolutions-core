package de.kobich.audiosolutions.core.service.play.player;

import java.io.File;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClient;
import de.kobich.audiosolutions.core.service.play.AudioPlayerClientDispatcher;
import de.kobich.audiosolutions.core.service.play.AudioPlayingThreadManager;
import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;

@Service
public class MockAudioPlayer extends AbstractAudioPlayer {
	private static final Logger logger = Logger.getLogger(MockAudioPlayer.class);
	private static final long COMPLETE_MILLIS = 30000;
	@Autowired
	private AudioPlayerState state;
	@Autowired
	private AudioPlayingThreadManager threadManager;
	@Autowired
	private AudioPlayerClientDispatcher dispatcher;
	
	@Override
	public AudioPlayerResponse playFile(File file, long beginMillis, AudioPlayerClient client) throws AudioException {
		logger.debug("Play file: " + file.getAbsolutePath());
		
		state.setPlaying(file, COMPLETE_MILLIS);

		System.out.println();
		boolean fireStopped = true;
		for (long playedMillis = beginMillis; playedMillis < COMPLETE_MILLIS; ) {
			// check player state
			Optional<AudioPlayerResponse> responseOpt = super.checkState(state, threadManager);
			if (responseOpt.isPresent()) {
				return responseOpt.get();
			}

			// play mock
			System.out.println("la-le-lü ");
			doSleep(500);
			playedMillis += 500;
			dispatcher.firePlayedMillis(client, playedMillis);
		}
		if (fireStopped) {
			state.requestStop();
		}
		return new AudioPlayerResponse(PlayListFlowType.NEXT_TRACK);
	}
}
