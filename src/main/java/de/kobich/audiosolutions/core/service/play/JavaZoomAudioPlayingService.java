package de.kobich.audiosolutions.core.service.play;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.play.player.JavaZoomAudioPlayer;

/**
 * Javazoom audio player.
 */
@Service(value=IAudioPlayingService.JAVA_ZOOM_PLAYER)
public class JavaZoomAudioPlayingService extends AbstractAudioPlayingService {
	@Autowired
	private JavaZoomAudioPlayer audioPlayer;
	
	@Override
	public synchronized void play(AudioPlayerClient client, IAudioPlayinglist playList) throws AudioException {
		super.play(this.audioPlayer, client, playList);
	}


}
