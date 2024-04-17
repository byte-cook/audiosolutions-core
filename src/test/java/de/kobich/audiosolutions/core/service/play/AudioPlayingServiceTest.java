package de.kobich.audiosolutions.core.service.play;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.play.player.IAudioPlayerListener;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioPlayingServiceTest {
	@Autowired
	@Qualifier(IAudioPlayingService.JAVA_ZOOM_PLAYER)
	private IAudioPlayingService playService;
	@Autowired
	@Qualifier(IAudioPlayingService.MOCK_PLAYER)
	private IAudioPlayingService mockPlayService;
	
	@Autowired
	private PlaylistService playlistService;
	
	@Test
	public void testEmptyPersistedPlayingList() {
		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList list = new PersistableAudioPlayingList(editablePlaylist);
		assertNull(list.getStartFile().orElse(null));
		assertNull(list.getCurrentFile().orElse(null));
		assertNull(list.getNextFile().orElse(null));
		assertNull(list.getPreviousFile().orElse(null));
	}
	
	@Test
	public void testPersistedPlayingList() {
		final File file1 = new File("file1.ogg").getAbsoluteFile();
		final File file2 = new File("file2.ogg").getAbsoluteFile();
		final File file3 = new File("file3.ogg").getAbsoluteFile();
		final File file4 = new File("file4.ogg").getAbsoluteFile();
		
		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList list = new PersistableAudioPlayingList(editablePlaylist);
		list.setLoopEnabled(true);

		// append file1 + file2
		list.appendFiles(Set.of(file1, file2));
		assertEquals(List.of(file1, file2), list.getFilesSorted().stream().map(EditablePlaylistFile::getFile).toList());
		assertEquals(file1, list.getNextFile().orElse(null));
		assertEquals(file1, list.getStartFile().orElse(null));
		// append file4
		list.appendFiles(Set.of(file4));
		assertEquals(List.of(file1, file2, file4), list.getFilesSorted().stream().map(EditablePlaylistFile::getFile).toList());
		assertEquals(file2, list.getNextFile().orElse(null));
		assertEquals(file2, list.getCurrentFile().orElse(null));
		// appendAfterCurrent file3
		list.appendFilesAfterCurrent(Set.of(file3));
		assertEquals(List.of(file1, file2, file3, file4), list.getFilesSorted().stream().map(EditablePlaylistFile::getFile).toList());
		list.getNextFile();
		assertEquals(file3, list.getCurrentFile().orElse(null));
		list.getNextFile();
		assertEquals(file4, list.getCurrentFile().orElse(null));
		list.getNextFile();
		assertEquals(file1, list.getCurrentFile().orElse(null));
		list.getPreviousFile();
		assertEquals(file4, list.getCurrentFile().orElse(null));
		list.getPreviousFile();
		assertEquals(file3, list.getCurrentFile().orElse(null));
		// remove file3
		EditablePlaylistFile eFile3 = list.getFilesSorted().get(2);
		assertEquals(file3, eFile3.getFile());
		list.removeFiles(Set.of(eFile3));
		assertEquals(List.of(file1, file2, file4), list.getFilesSorted().stream().map(EditablePlaylistFile::getFile).toList());
		assertEquals(file3, list.getCurrentFile().orElse(null));
		assertEquals(file1, list.getNextFile().orElse(null));
		assertEquals(file2, list.getNextFile().orElse(null));
		// remove file2
		EditablePlaylistFile eFile2 = list.getFilesSorted().get(1);
		assertEquals(file2, eFile2.getFile());
		list.removeFiles(Set.of(eFile2));
		assertEquals(List.of(file1, file4), list.getFilesSorted().stream().map(EditablePlaylistFile::getFile).toList());
		assertEquals(file2, list.getCurrentFile().orElse(null));
		assertEquals(file1, list.getPreviousFile().orElse(null));
		list.setStartFile(list.getFilesSorted().get(1));
		assertEquals(file4, list.getCurrentFile().orElse(null));
	}
	
	@Test
	public void testPersistedPlayingListDuplicates() {
		final File file1 = new File("file1.ogg").getAbsoluteFile();
		final File file2 = new File("file2.ogg").getAbsoluteFile();

		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList list = new PersistableAudioPlayingList(editablePlaylist);
		list.appendFiles(Set.of(file1, file2));
		assertEquals(2, list.getFilesSorted().size());
		list.appendFiles(Set.of(file1, file2));
		list.getFilesSorted().forEach(f -> System.out.println(f));
		assertEquals(2, list.getFiles().size());
		assertEquals(2, list.getFilesSorted().size());
	}
	
	@Test
	public void testPersistedPlayingListLoop() {
		final File file1 = new File("file1.ogg").getAbsoluteFile();
		final File file2 = new File("file2.ogg").getAbsoluteFile();

		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList list = new PersistableAudioPlayingList(editablePlaylist);
		list.setLoopEnabled(true);
		list.appendFiles(Set.of(file1, file2));
		assertEquals(file1, list.getStartFile().orElse(null));
		assertEquals(file2, list.getNextFile().orElse(null));
		assertEquals(file1, list.getNextFile().orElse(null));
		assertEquals(file2, list.getNextFile().orElse(null));
		assertEquals(file1, list.getPreviousFile().orElse(null));
		assertEquals(file2, list.getPreviousFile().orElse(null));
		assertEquals(file1, list.getPreviousFile().orElse(null));
		
		list.setLoopEnabled(false);
		assertEquals(file2, list.getNextFile().orElse(null));
		assertTrue(list.getNextFile().isEmpty());
	}
	
	@Test
	public void testMockPlaying() throws Exception {
		File testFile1 = new File(AudioPlayingServiceTest.class.getResource("/mp3/01-mp3-no-tags.mp3").toURI()).getAbsoluteFile();
		File testFile2 = new File(AudioPlayingServiceTest.class.getResource("/mp3/02-mp3-no-tags.mp3").toURI()).getAbsoluteFile();
		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList playlist = new PersistableAudioPlayingList(editablePlaylist);
		playlist.appendFiles(Set.of(testFile1, testFile2));
		
		TestAudioPlayerListener l = new TestAudioPlayerListener();
		AudioPlayerClient client = new AudioPlayerClient("test");
		client.getListenerList().addListener(l);
		
		mockPlayService.play(client, playlist);
		assertTrue(l.waitForPlayedAndReset(5000));
		assertEquals(testFile1, playlist.getCurrentFile().orElse(null));
		mockPlayService.stop(client);
		assertTrue(l.waitForStoppedAndReset(5000));
		assertEquals(testFile1, playlist.getCurrentFile().orElse(null));
		mockPlayService.play(client, playlist);
		assertTrue(l.waitForPlayedAndReset(5000));
		assertEquals(testFile1, playlist.getCurrentFile().orElse(null));
		mockPlayService.next(client);
		assertTrue(l.waitForPlayedAndReset(5000));
		assertEquals(testFile2, playlist.getCurrentFile().orElse(null));
		mockPlayService.stop(client);
		assertTrue(l.waitForStoppedAndReset(5000));
	}
	
	@Test
	public void testPlaying() throws Exception {
		File testFile = new File(AudioPlayingServiceTest.class.getResource("/mp3/01-mp3-no-tags.mp3").toURI());
		EditablePlaylist editablePlaylist = playlistService.createNewPlaylist("_", true);
		PersistableAudioPlayingList playlist = new PersistableAudioPlayingList(editablePlaylist);
		playlist.appendFiles(Set.of(testFile));
		
		TestAudioPlayerListener l = new TestAudioPlayerListener();
		AudioPlayerClient client = new AudioPlayerClient("test");
		client.getListenerList().addListener(l);
		playService.play(client, playlist);
		
		assertTrue(l.waitForPlayedAndReset(10000));
		assertTrue(l.waitForStoppedAndReset(30000));
	}
	
	private static class TestAudioPlayerListener implements IAudioPlayerListener {
		private CountDownLatch playLatch = new CountDownLatch(1);
		private CountDownLatch stopLatch = new CountDownLatch(1);

		@Override
		public void play(File file, long totalMillis) {
			System.out.println("======== Start playing: %s".formatted(file.getName()));
			playLatch.countDown();
		}

		@Override
		public void resume() {
		}

		@Override
		public void stopped() {
			System.out.println("======== Stopped");
			stopLatch.countDown();
		}

		@Override
		public void paused() {
		}

		@Override
		public void playedMillis(long millis) {
			System.out.println("================ PlayedMillis: " + millis);
		}

		@Override
		public void errorOccured(AudioException exc) {
			throw new IllegalStateException(exc);
		}

		public boolean waitForPlayedAndReset(long timeoutMillis) throws InterruptedException {
			try {
				playLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
				return playLatch.getCount() == 0;
			}
			finally {
				playLatch = new CountDownLatch(1);
			}
		}
		
		public boolean waitForStoppedAndReset(long timeoutMillis) throws InterruptedException {
			try {
				stopLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
				return stopLatch.getCount() == 0;
			}
			finally {
				stopLatch = new CountDownLatch(1);
			}
		}
	}

}
