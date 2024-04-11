package de.kobich.audiosolutions.core.service.play;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.player.MockAudioPlayer;

@Service(value=IAudioPlayingService.MOCK_PLAYER)
public class MockAudioPlayingService extends AbstractAudioPlayingService {
	@Autowired
	private MockAudioPlayer audioPlayer;

	@Override
	public synchronized void play(AudioPlayerClient client, IAudioPlayinglist playList) throws AudioException {
		super.play(audioPlayer, client, playList);
	}
	
}
