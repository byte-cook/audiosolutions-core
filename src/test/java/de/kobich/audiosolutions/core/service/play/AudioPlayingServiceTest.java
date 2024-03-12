package de.kobich.audiosolutions.core.service.play;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.mp3.id3.FileID3TagServiceTest;
import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.component.file.FileDescriptor;
import javazoom.jl.decoder.JavaLayerException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioPlayingServiceTest {
	private static FileDescriptor testFile;
	@Autowired
	@Qualifier(IAudioPlayingService.JAVA_ZOOM_PLAYER)
	private IAudioPlayingService playService;
	
	@BeforeAll
	public static void init() throws URISyntaxException {
		File file = new File(FileID3TagServiceTest.class.getResource("/mp3/mp3-no-tags.mp3").toURI());
		AudioPlayingServiceTest.testFile = new FileDescriptor(file, "/mp3/mp3-no-tags.mp3");
	}
	
	@Test
	public void testPlaying() throws AudioException, FileNotFoundException, IOException, JavaLayerException, InterruptedException {
		TestAudioPlayerListener l = new TestAudioPlayerListener();
		AudioPlayerClient client = new AudioPlayerClient("test");
		client.getListenerList().addListener(l);
		AudioPlayList playList = new AudioPlayList();
		playList.addFile(testFile);
		playService.play(client, playList);
		
		assertTrue(l.isSuccessful());
	}
	
	private static class TestAudioPlayerListener implements IAudioPlayerListener {
		private final CountDownLatch playLatch = new CountDownLatch(1);
		private final CountDownLatch stoppedLatch = new CountDownLatch(1);

		@Override
		public void play(File file, long totalMillis) {
			playLatch.countDown();
		}

		@Override
		public void resume(File file) {
		}

		@Override
		public void stopped() {
			stoppedLatch.countDown();
		}

		@Override
		public void paused() {
		}

		@Override
		public void playedMillis(long millis) {
			System.out.println("PlayedMillis: " + millis);
		}

		@Override
		public void errorOccured(AudioException exc) {
			throw new IllegalStateException(exc);
		}

		@Override
		public void playListModified() {
		}
		
		public boolean isSuccessful() throws InterruptedException {
			playLatch.await(10, TimeUnit.SECONDS);
			stoppedLatch.await(30, TimeUnit.SECONDS);
			long total = playLatch.getCount() + stoppedLatch.getCount();
			return total == 0;
		}
		
	}

}
