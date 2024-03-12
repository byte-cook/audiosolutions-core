package de.kobich.audiosolutions.core.service.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.mp3.id3.FileID3TagServiceTest;
import de.kobich.audiosolutions.core.service.mp3.id3.ID3TagVersion;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorResult;
import de.kobich.component.file.descriptor.FileDescriptorService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioDataServiceTest {
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioDataService dataService;
	@Autowired
	private FileDescriptorService fileService;
	@Autowired
	private AudioPersistenceService persistenceService;
	@Autowired
	private IFileID3TagService id3TagService;
	
	@AfterEach
	public void afterEach() throws AudioException {
		persistenceService.removeAll();
	}
	
	@Test
	public void testAddAudioData() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("satisfaction.mp3", "satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("factory girl.mp3", "factory girl.mp3");
		
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist(TestUtils.STONES).track("satisfactionXX").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist(TestUtils.STONES).track("factory girl").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertTrue(file1.hasMetaData());
		AudioData ad1 = file1.getMetaData(AudioData.class);
		assertEquals(TestUtils.STONES, ad1.getArtist().get());
		assertEquals("satisfactionXX", ad1.getTrack().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad1.getMedium().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad1.getState());
		
		changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).track("satisfaction").medium("cd 1").album("Best of").genre("Rock").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertEquals("satisfaction", ad1.getTrack().get());
		assertEquals(AudioState.TRANSIENT, ad1.getState());

		changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).artistRemove(true).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertEquals("satisfaction", ad1.getTrack().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad1.getArtist().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad1.getState());
	}
	
	@Test
	public void testApplyChanges() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("satisfaction.mp3", "satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("factory girl.mp3", "factory girl.mp3");
		
		Set<AudioDataChange> changes = Set.of(
			AudioDataChange.builder().fileDescriptor(file1).artist(TestUtils.STONES).track("satisfactionXX").build(),
			AudioDataChange.builder().fileDescriptor(file2).artist(TestUtils.STONES).track("factory girl").build()
		);
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertTrue(file1.hasMetaData());
		AudioData ad1 = file1.getMetaData(AudioData.class);
		assertEquals(TestUtils.STONES, ad1.getArtist().get());
		assertEquals("satisfactionXX", ad1.getTrack().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad1.getMedium().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad1.getState());
		
		changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).track("satisfaction").medium("cd 1").album("Best of").genre("Rock").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertEquals("satisfaction", ad1.getTrack().get());
		assertEquals(AudioState.TRANSIENT, ad1.getState());

		changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).artistRemove(true).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertEquals("satisfaction", ad1.getTrack().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad1.getArtist().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad1.getState());
	}
	
	@Test
	public void testApplySingleChange() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("satisfaction.mp3", "satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("factory girl.mp3", "factory girl.mp3");
		Set<FileDescriptor> files = Set.of(file1, file2);

		dataService.applyChanges(files, AudioDataChange.builder().artist(TestUtils.STONES).build(), PROGRESS_MONITOR);
		assertTrue(file1.hasMetaData());
		for (FileDescriptor file : files) {
			AudioData ad = file.getMetaData(AudioData.class);
			assertEquals(TestUtils.STONES, ad.getArtist().get());
			assertEquals(AudioData.DEFAULT_VALUE, ad.getMedium().get());
			assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad.getState());
		}
	}

	@Test
	public void testAudioState() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("satisfaction.mp3", "satisfaction.mp3");
		Set<FileDescriptor> files = Set.of(file1);

		dataService.applyChanges(files, AudioDataChange.builder().artist(TestUtils.STONES).build(), PROGRESS_MONITOR);
		assertTrue(file1.hasMetaData());
		AudioData ad = file1.getMetaData(AudioData.class);
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad.getState());

		dataService.applyChanges(files, AudioDataChange.builder().albumIdentity(AlbumIdentity.createNew()).build(), PROGRESS_MONITOR);
		ad = file1.getMetaData(AudioData.class);
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad.getState());
		
		persistenceService.persist(files, PROGRESS_MONITOR);
		dataService.applyChanges(files, AudioDataChange.builder().albumIdentity(AlbumIdentity.createNew()).build(), PROGRESS_MONITOR);
		ad = file1.getMetaData(AudioData.class);
		assertEquals(AudioState.PERSISTENT_MODIFIED_INCOMPLETE, ad.getState());
	}
	
	@Test
	public void testRemoveAudioData() throws Exception {
		final Date albumPublication = new GregorianCalendar(1981, Calendar.MARCH, 12).getTime();
		FileDescriptor file = TestUtils.createFileDescriptor("satisfaction.mp3", "satisfaction.mp3");
		Set<AudioDataChange> changes = Set.of(AudioDataChange.builder().fileDescriptor(file).artist(TestUtils.STONES).track("satisfaction").albumPublication(albumPublication).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);

		changes = Set.of(AudioDataChange.builder().fileDescriptor(file).artistRemove(true).albumPublicationRemove(true).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertTrue(file.hasMetaData());
		AudioData ad = file.getMetaData(AudioData.class);
		assertNull(ad.getAlbumPublication().orElse(null));
		assertEquals(AudioData.DEFAULT_VALUE, ad.getArtist().get());
		assertEquals("satisfaction", ad.getTrack().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad.getState());
	}

	@Test
	public void testAddAudioDataByStructure() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/memory motel.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/The Who/bargain.mp3");
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/The Who/Best Of/bargain.mp3");
		final Set<FileDescriptor> audioFiles = Set.of(file1, file2, file3);
		
		dataService.addAudioDataByStructure(audioFiles, "/<>/<artist>/<album>/<track>.mp3", PROGRESS_MONITOR);
		assertFalse(file1.hasMetaData());
		assertFalse(file2.hasMetaData());
		assertTrue(file3.hasMetaData());
		AudioData ad3 = file3.getMetaData(AudioData.class);
		assertEquals("The Who", ad3.getArtist().get());
		assertEquals("Best Of", ad3.getAlbum().get());
		assertEquals("bargain", ad3.getTrack().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad3.getState());

		// correct pattern
		dataService.addAudioDataByStructure(audioFiles, "/<>/<artist>/<track>.mp3", PROGRESS_MONITOR);
		assertTrue(file1.hasMetaData());
		AudioData ad1 = file1.getMetaData(AudioData.class);
		assertEquals("Rolling Stones", ad1.getArtist().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad1.getAlbum().get());
		assertEquals("memory motel", ad1.getTrack().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad1.getState());
		assertTrue(file2.hasMetaData());
		AudioData ad2 = file2.getMetaData(AudioData.class);
		assertEquals("The Who", ad2.getArtist().get());
		assertEquals(AudioData.DEFAULT_VALUE, ad2.getAlbum().get());
		assertEquals("bargain", ad2.getTrack().get());
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, ad2.getState());
	}

	@Test
	public void testAddAudioDataByID3Tags() throws Exception {
		File file = new File(FileID3TagServiceTest.class.getResource("/mp3/mp3-no-tags.mp3").toURI());
		FileDescriptor fd = new FileDescriptor(file, "/Rolling Stones/Beggars Banquet/07-sympathy for the devil.mp3");
		AudioFileResult result = id3TagService.writeID3TagsByStructure(Set.of(fd), ID3TagVersion.ALL, "/<artist>/<album>/<trackNo>-<track>.mp3", PROGRESS_MONITOR);
		Set<FileDescriptor> files = result.getSucceededFiles();
		dataService.addAudioDataByID3Tags(files, null);
		assertFalse(files.isEmpty());
		fd = files.iterator().next();
		AudioData ad = fd.getMetaData(AudioData.class);
		assertNotNull(ad);
		assertEquals("Rolling Stones", ad.getArtist().orElse(null));
		assertEquals("Beggars Banquet", ad.getAlbum().orElse(null));
		assertEquals("sympathy for the devil", ad.getTrack().orElse(null));
		assertEquals(7, ad.getTrackNo().orElse(-1));
	}
	
	@Test
	public void testModifyImportDirectories() throws Exception {
		final File rootFolder = new File(AudioDataServiceTest.class.getResource("/files/").toURI());
		
		File importFolder1 = new File(rootFolder, "folder1");
		Set<FileDescriptor> files = fileService.readFiles(importFolder1, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertEquals(10, files.size());
		
		// files are not persistent
		File importFolder2 = new File(rootFolder, "folder2");
		FileDescriptorResult res = dataService.modifyImportDirectories(files, importFolder2, PROGRESS_MONITOR);
		assertEquals(10, res.getFailedFiles().size());
		
		// persist + modify directory
		dataService.applyChanges(files, AudioDataChange.builder().genre("Test").build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		res = dataService.modifyImportDirectories(files, importFolder2, PROGRESS_MONITOR);
		assertEquals(10, res.getReplacedFiles().size());
		
		// folder does not exist
		File importFolder3 = new File(rootFolder, "not existing folder");
		res = dataService.modifyImportDirectories(files, importFolder3, PROGRESS_MONITOR);
		assertEquals(10, res.getFailedFiles().size());
	}
}
