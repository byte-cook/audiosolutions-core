package de.kobich.audiosolutions.core.service.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataBuilder;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptor;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchResult;
import de.kobich.audiosolutions.core.service.search.AudioTextSearchService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioPersistenceServiceTest {
	private static final Logger logger = Logger.getLogger(AudioPersistenceServiceTest.class);
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioPersistenceService persistenceService;
	@Autowired
	private AudioSearchService searchService;
	@Autowired
	private AudioTextSearchService textSearchService;
	@Autowired
	private AudioDataService dataService;
	
	@BeforeAll
	static void init() {
		BasicConfigurator.configure();
	}
	
	@AfterAll
	public static void clean(@Autowired AudioPersistenceService persistenceService) throws AudioException {
		persistenceService.removeAll();
	}
	
	@BeforeEach
	void beforeEach() throws AudioException {
		logger.info("====================================");
		logger.info("Before each: delete all");
		persistenceService.removeAll();
	}

	@Test
	void isEmpty() throws Exception {
		assertEquals(0, persistenceService.getCount(AudioAttribute.TRACK));
	}
	
	@Test
	void insert1withEmptyPublication() throws Exception {
		AudioFileDescriptor file1 = TestUtils.createAudioFileDescriptor("/cdrom/Rolling Stones/memory motel.mp3", AudioDataBuilder.builder().artist("Rolling Stones").track("memory motel"));
		file1.getAudioDataValues().put(AudioAttribute.ALBUM_PUBLICATION, "");
		Set<AudioFileDescriptor> audioFiles = Set.of(file1);
		dataService.addAudioData(audioFiles, PROGRESS_MONITOR);
		List<FileDescriptor> files = audioFiles.stream().map(AudioFileDescriptor::getFileDescriptor).toList();
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		AudioData ad = file1.getFileDescriptor().getMetaData(AudioData.class);
		assertNotNull(ad);
		assertTrue(ad.getAlbumDescription().isEmpty());
	}

	@Test
	void insert1_removeTwice() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));

		dataService.removeAudioData(files, PROGRESS_MONITOR);
		AudioData ad = files.iterator().next().getMetaData(AudioData.class);
		assertEquals(AudioState.REMOVED, ad.getState());
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(0, persistenceService.getCount(AudioAttribute.TRACK));
		assertNull(files.iterator().next().getMetaData(AudioData.class));

		dataService.removeAudioData(files, PROGRESS_MONITOR);
		assertNull(files.iterator().next().getMetaData(AudioData.class));
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(0, persistenceService.getCount(AudioAttribute.TRACK));
		assertNull(files.iterator().next().getMetaData(AudioData.class));
	}

	@Test
	void insert2() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/start me up.mp3").artist("Rolling Stones").track("start me up").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
	}
	
	@Test
	void insert2_delete2() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/start me up.mp3").artist("Rolling Stones").track("start me up").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(0, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(0, persistenceService.getCount(AudioAttribute.ARTIST));
	}
	
	@Test
	void insert2_delete1_checkOrphaned() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/bargain.mp3").artist("The Who").track("bargain").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		
		Set<FileDescriptor> foundFiles = searchService.searchByArtists(Set.of("The Who"), PROGRESS_MONITOR);
		assertEquals(1, foundFiles.size());
		dataService.removeAudioData(foundFiles, PROGRESS_MONITOR);
		persistenceService.persist(foundFiles, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}
	
	@Test
	void insert2_delete1_checkOrphanedMediumGenre() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").medium("cd 1").genre("Blues").artist("Rolling Stones").track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/bargain.mp3").medium("cd 2").genre("Rock").artist("The Who").track("bargain").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		
		Set<FileDescriptor> foundFiles = searchService.searchByArtists(Set.of("The Who"), PROGRESS_MONITOR);
		assertEquals(1, foundFiles.size());
		dataService.removeAudioData(foundFiles, PROGRESS_MONITOR);
		persistenceService.persist(foundFiles, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}

	@Test
	void insertMany_delete1_checkOrphaned() throws Exception {
		final int COUNT = 2000;
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/bargain.mp3").artist("The Who").track("bargain").build());
		for (int i = 0; i < COUNT; ++i) {
			changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/track "+i+".mp3").artist("Rolling Stones").track("track " + i).build());
		}
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(COUNT+1, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		
		Set<FileDescriptor> foundFiles = searchService.searchByArtists(Set.of("The Who"), PROGRESS_MONITOR);
		assertEquals(1, foundFiles.size());
		dataService.removeAudioData(foundFiles, PROGRESS_MONITOR);
		persistenceService.persist(foundFiles, PROGRESS_MONITOR);
		assertEquals(COUNT, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}

	@Test
	void insertMany_errorSameFileInSecondPartition() throws Exception {
		// insert one file
		final String fileName = String.format("track %04d.mp3", 1007);
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/"+fileName).artist("Rolling Stones").track(fileName).build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));

		// insert bulk with duplicate file
		changes.clear();
		final int COUNT = 1100;
		for (int i = 0; i < COUNT; ++i) {
			String iFileName = String.format("track %04d.mp3", i);
			changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/"+iFileName).artist("Rolling Stones").track(iFileName).build());
		}
		Set<FileDescriptor> bulkFiles = dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertThrows(AudioException.class, () -> persistenceService.persist(bulkFiles, PROGRESS_MONITOR));
		// the first partition with 1000 files could be inserted
		assertEquals(1001, persistenceService.getCount(AudioAttribute.TRACK));
		FileDescriptor file77 = bulkFiles.stream().filter(f -> f.getFileName().equals("track 0077.mp3")).findFirst().orElse(null);
		assertNotNull(file77);
		assertEquals(AudioState.PERSISTENT_INCOMPLETE, file77.getMetaData(AudioData.class).getState());
		FileDescriptor file1077 = bulkFiles.stream().filter(f -> f.getFileName().equals("track 1077.mp3")).findFirst().orElse(null);
		assertNotNull(file1077);
		assertEquals(AudioState.TRANSIENT_INCOMPLETE, file1077.getMetaData(AudioData.class).getState());
	}
	
	@Test
	void insert2_errorSameFile() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		
		changes.clear();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/angry.mp3").artist("The Rolling Stones").track("angry").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("The Rolling Stones").track("memory motel").build());
		final Set<FileDescriptor> duplicateFiles = dataService.applyChanges(changes, PROGRESS_MONITOR);
		AudioException exc = assertThrows(AudioException.class, () -> persistenceService.persist(duplicateFiles, PROGRESS_MONITOR));
		assertEquals(AudioException.DUPLICATE_FILE_ERROR, exc.getErrorCode());
		changes.forEach(f -> assertFalse(f.getFileDescriptor().getMetaData(AudioData.class).getState().isPersistent()));

		Set<FileDescriptor> foundedTracks = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		System.out.println("Tracks in DB:");
		foundedTracks.forEach(t -> System.out.println(t.getFileName()));
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
	}

	@Test
	void insert2WithSameFile_checkAudioData() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		
		changes.clear();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Beatles/yesterday.mp3").artist("Beatles").track("yesterday").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("The Rolling Stones").track("memory motel").build());
		final Set<FileDescriptor> duplicateFiles = dataService.applyChanges(changes, PROGRESS_MONITOR);
		assertThrows(AudioException.class, () -> persistenceService.persist(duplicateFiles, PROGRESS_MONITOR));
		
		Set<FileDescriptor> foundedTracks = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		foundedTracks.forEach(t -> System.out.println(t.getFileName()));
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		
		for (FileDescriptor fd : duplicateFiles) {
			switch (fd.getFileName()) {
			case "yesterday.mp3":
			case "memory motel.mp3":
				assertEquals(AudioState.TRANSIENT_INCOMPLETE, fd.getMetaData(AudioData.class).getState());
				break;
			}
		}
	}

	@Test
	void insert1_update1_checkAlbumPublication() throws Exception {
		// insert
		final Date albumPublication = new GregorianCalendar(1968, Calendar.DECEMBER, 6).getTime();
		Set<FileDescriptor> files = new HashSet<>();
		files.add(TestUtils.createFileDescriptor("/cdrom/Rolling Stones/sympathy for the devil.mp3", AudioDataBuilder.builder().artist("Rolling Stones").album("Beggars Banquet").albumPublication(albumPublication).track("sympathy for the devil")));
		files.add(TestUtils.createFileDescriptor("/cdrom/Rolling Stones/dear doctor.mp3", AudioDataBuilder.builder().artist("Rolling Stones").album("Beggars Banquet").track("sympathy for the devil")));
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		Set<FileDescriptor> foundedTracks = searchService.search(AudioSearchQuery.builder().artistName("Rolling Stones").build(), PROGRESS_MONITOR);
		assertFalse(foundedTracks.isEmpty());
		AudioData audioData = foundedTracks.iterator().next().getMetaData(AudioData.class);
		assertEquals(albumPublication, audioData.getAlbumPublication().get());

		// update
		dataService.applyChanges(foundedTracks, AudioDataChange.builder().albumPublicationRemove(true).build(), PROGRESS_MONITOR);
		persistenceService.persist(foundedTracks, PROGRESS_MONITOR);
		foundedTracks = searchService.search(AudioSearchQuery.builder().artistName("Rolling Stones").build(), PROGRESS_MONITOR);
		audioData = foundedTracks.iterator().next().getMetaData(AudioData.class);
		assertFalse(audioData.getAlbumPublication().isPresent());
	}

	@Test
	void insert2WithEqualAudioData() throws Exception {
		AudioDataChange file1 = TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build();
		AudioDataChange file2 = TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/memory motel - Rolling Stones.mp3").artist("Rolling Stones").track("memory motel").build();
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(file1);
		changes.add(file2);
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		// remove
		files.clear();
		files = Set.of(file2.getFileDescriptor());
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.TRACK));
		// add again
		dataService.applyChanges(Set.of(file2), PROGRESS_MONITOR);
		files = persistenceService.persist(Set.of(file2.getFileDescriptor()), PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}

	@Test
	void insert3_update2() throws Exception {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").artist("Rolling Stones").track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/start me up.mp3").artist("Rolling Stones").track("start me up").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/bargain.mp3").artist("The Who").track("bargain").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		files = persistenceService.persist(files, PROGRESS_MONITOR);
		
		Set<FileDescriptor> foundFiles = searchService.searchByArtists(Set.of("Rolling Stones"), PROGRESS_MONITOR);
		assertEquals(2, foundFiles.size());
		Set<AudioDataChange> updateChanges = new HashSet<>();
		System.out.println("Changing artist to: The Who");
		for (FileDescriptor fd : foundFiles) {
			updateChanges.add(AudioDataChange.builder().fileDescriptor(fd).artist("The Who").build());
		}
		dataService.applyChanges(updateChanges, PROGRESS_MONITOR);
		persistenceService.persist(foundFiles, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}

	@Test
	void testAlbumCollection() throws Exception {
		final String album = "Best Of Rock";
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/01-start me up - Rolling Stones.mp3").artist("Rolling Stones").album(album).track("start me up").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/02-bargain - The Who.mp3").artist("The Who").album(album).track("bargain").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/03-back in black - AC DC.mp3").artist("AC DC").album(album).track("back in black").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/04-satisfaction - Rolling Stones.mp3").artist("Rolling Stones").album(album).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		changes.stream().map(AudioDataChange::getFileDescriptor).collect(Collectors.toSet());
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(4, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(3, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}

	@Test
	void testAlbumCollectionWithDisks() throws Exception {
		final String album = "Best Of Rock";
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/Disk 1/01-start me up - Rolling Stones.mp3").artist("Rolling Stones").album(album).track("start me up").disk("Disk 1").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/Disk 2/01-bargain - The Who.mp3").artist("The Who").album(album).track("bargain").disk("Disk 2").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Various Artists/Best Of Rock/Disk 2/02-highway to hell - AC DC.mp3").artist("AC DC").album(album).track("highway to hell").disk("Disk 2").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		changes.stream().map(AudioDataChange::getFileDescriptor).collect(Collectors.toSet());
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(3, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
	}
	
	@Test
	void testSameNamedAlbums() throws Exception {
		final String album = "Best Of";
		// Rolling Stones' Best Of
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Best Of/01-start me up.mp3").artist("Rolling Stones").album(album).track("start me up").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Best Of/02-satisfaction.mp3").artist("Rolling Stones").album(album).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));

		// The Who's Best Of 
		changes.clear();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/Best Of/01-bargain.mp3").artist("The Who").album(album).track("bargain").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/Best Of/02-who are you.mp3").artist("The Who").album(album).track("who are you").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(4, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));

		// Rolling Stones' Best Of (add file)
		changes.clear();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Best Of/03-don't stop.mp3").artist("Rolling Stones").album(album).track("don't stop").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(5, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
	}
	
	@Test
	void testAlbumArtistWithSameName() throws Exception {
		final String album = "Best Of Rock";
		
		// Best Of Rock: Collection of different artists
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Best Of Rock/01-start me up - Rolling Stones.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Best Of Rock/02-bargain - The Who.mp3");
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).track("start me up").build(), 
				AudioDataChange.builder().fileDescriptor(file2).artist("The Who").album(album).track("bargain").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		AudioTextSearchResult result = this.textSearchService.search("album: best", 10, null);
		assertEquals(1, result.getAlbums().size());
		assertFalse(result.getAlbums().get(0).getArtist().isPresent());
		
		// Best Of Rock: Rolling Stones album
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of Rock/01-start me up.mp3");
		FileDescriptor file4 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of Rock/02-satisfaction.mp3");
		changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file3).artist("Rolling Stones").album(album).track("start me up").build(), 
				AudioDataChange.builder().fileDescriptor(file4).artist("Rolling Stones").album(album).track("satisfaction").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		result = this.textSearchService.search("album: best", 10, null);
		assertEquals(2, result.getAlbums().size());
		Album album1 = result.getAlbums().get(0);
		Album album2 = result.getAlbums().get(1);
		assertFalse(album1.getArtist().isPresent() && album2.getArtist().isPresent());
		assertTrue(album1.getArtist().isPresent() || album2.getArtist().isPresent());
		Album albumWithArtist = album1.getArtist().isPresent() ? album1 : album2;
		assertEquals("Rolling Stones", albumWithArtist.getArtist().get().getName());
	}

	@Test
	void testAlbumArtist() throws Exception {
		final String album = "Best Of";
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of 2/02-satisfaction.mp3");
		
		// standard album
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).track("start me up").build(), 
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		AudioTextSearchResult result = this.textSearchService.search("artist: stones", 10, null);
		assertEquals(2, result.getAlbums().size());
		assertTrue(result.getAlbums().get(0).getArtist().isPresent());
		assertEquals("Rolling Stones", result.getAlbums().get(0).getArtist().get().getName());
//		table track:
//		albumId | path
//		1       | /cdrom/Rolling Stones/Best Of/..
//		2       | /cdrom/Rolling Stones/Best Of 2/..
		
		// collection album
		AlbumIdentity id = AlbumIdentity.createNew();
		changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).albumIdentity(id).build(),
				AudioDataChange.builder().fileDescriptor(file2).albumIdentity(id).build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		result = this.textSearchService.search("artist: stones", 10, null);
		assertEquals(1, result.getAlbums().size());
		assertTrue(result.getAlbums().get(0).getArtist().isPresent());
//		table track:
//		albumId | path
//		3       | /cdrom/Rolling Stones/Best Of/..
//		3       | /cdrom/Rolling Stones/Best Of 2/..
		
		// switch back to standard album
		changes = Set.of(
				// The albumIdentity must be set here. Otherwise, the album with id 3 is used for both files 
				// because both parent paths are available for this album.
				// 
				// By setting the albumIdentity for files in different folders, these folders are now linked together in an album. 
				// To separate the folders from each other again, a new albumIdentity must be explicitly assigned. 
				AudioDataChange.builder().fileDescriptor(file1).albumIdentityRemove(true).albumIdentity(AlbumIdentity.createNew()).build(),
				AudioDataChange.builder().fileDescriptor(file2).albumIdentityRemove(true).build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		result = this.textSearchService.search("artist: stones", 10, null);
		assertEquals(2, result.getAlbums().size());
		assertTrue(result.getAlbums().get(0).getArtist().isPresent());
	}
	
	@Test
	void testAlbumArtistForCollection() throws Exception {
		final String album = "Best Of Rock";
		
		// Best Of Rock: Collection of the same artist
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Best Of Rock/01-start me up - Rolling Stones.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Best Of Rock/03-saticfaction - Rolling Stones.mp3");
		
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).track("start me up").build(), 
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).track("saticfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		AudioTextSearchResult result = this.textSearchService.search("album: best", 10, null);
		assertEquals(1, result.getAlbums().size());
		assertTrue(result.getAlbums().get(0).getArtist().isPresent());

		// add new file with different artist
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/Best Of Rock/03-bargain - The Who.mp3");
		changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file3).artist("The Who").album(album).track("bargain").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		result = this.textSearchService.search("album: best", 10, null);
		assertEquals(1, result.getAlbums().size());
		assertFalse(result.getAlbums().get(0).getArtist().isPresent());
	}
	
	@Test
	void testAlbumIdentityWithDifferentFolders() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Grammy 2000/01-track 2000.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Grammy 2000/02-track 2000.mp3");
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/Grammy 2001/01-track 2001.mp3");
		FileDescriptor file4 = TestUtils.createFileDescriptor("/cdrom/Grammy 2001/02-track 2001.mp3");

		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).album("Grammy 2000").track("01-track 2000").albumIdentity(AlbumIdentity.createNew()).build(),
				AudioDataChange.builder().fileDescriptor(file2).album("Grammy 2000").track("02-track 2000").albumIdentity(AlbumIdentity.createNew()).build(),
				AudioDataChange.builder().fileDescriptor(file3).album("Grammy 2001").track("02-track 2001").albumIdentity(AlbumIdentity.createNew()).build(),
				AudioDataChange.builder().fileDescriptor(file4).album("Grammy 2001").track("02-track 2001").albumIdentity(AlbumIdentity.createNew()).build()
				);
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(4, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(4, persistenceService.getCount(AudioAttribute.ALBUM));
		
		// create two albums because there are two different album names 
		files = dataService.applyChanges(Set.of(file1, file2, file3, file4), AudioDataChange.builder().albumIdentity(AlbumIdentity.createNew()).build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(4, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
	}
	
	@Test
	void testAlbumIdentity() throws Exception {
		final String album = "Best Of";
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/02-satisfaction.mp3");
		
		// file are in different folders: two albums
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).track("start me up").build());
		changes.add(AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));

		// use the same album for all files
		changes.clear();
		AlbumIdentity id = AlbumIdentity.createNew();
		changes.add(AudioDataChange.builder().fileDescriptor(file1).albumIdentity(id).build());
		changes.add(AudioDataChange.builder().fileDescriptor(file2).albumIdentity(id).build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));

		// use a different album for each files
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file1).albumIdentity(AlbumIdentity.createNew()).build());
		changes.add(AudioDataChange.builder().fileDescriptor(file2).albumIdentity(AlbumIdentity.createNew()).build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));

		// use default behavior
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file1).albumIdentityRemove(true).build());
		changes.add(AudioDataChange.builder().fileDescriptor(file2).albumIdentityRemove(true).build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
	}

	@Test
	void testAlbumCountForDifferentParentFolders() throws Exception {
		final String album = "Best Of";
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/02-satisfaction.mp3");
		
		// without AlbumIdentity: two albums are created
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
	}

	@Test
	void testSameAlbumIdentifierWithDifferentAlbumNames() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/02-satisfaction.mp3");

		AlbumIdentity id = AlbumIdentity.createNew();
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album("Best Of 1").albumIdentity(id).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album("Best Of 2").albumIdentity(id).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
	}

	@Test
	void testSameAlbumIdentifierAfterSearch() throws Exception {
		final String album = "Best Of";
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/02-satisfaction.mp3");

		// with AlbumIdentifier: one album is created
		AlbumIdentity id = AlbumIdentity.createNew();
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).albumIdentity(id).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).albumIdentity(id).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		
		// search for these files and change medium: no new album should be created
		Set<FileDescriptor> foundedFiles = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		files = dataService.applyChanges(foundedFiles, AudioDataChange.builder().medium("cd 7").build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
	}

	@Test
	void testDifferentAlbumIdentifierAfterSearch() throws Exception {
		final String album = "Best Of";
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/01-start me up.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/Best Of/02-satisfaction.mp3");
		
		// with AlbumIdentifier: one album is created
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).artist("Rolling Stones").album(album).albumIdentity(AlbumIdentity.createNew()).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist("Rolling Stones").album(album).albumIdentity(AlbumIdentity.createNew()).track("satisfaction").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		
		// search for these files and change medium: album count should remain the same
		Set<FileDescriptor> foundedFiles = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		files = dataService.applyChanges(foundedFiles, AudioDataChange.builder().medium("hdd2").build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
	}
	
	@Test
	void testPathWithSQLWildcard() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Artist/track 1.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/A%/my song.mp3");
		
		Set<AudioDataChange> changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).artist("Artist").track("track 1").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);

		// SQL wildcards must be escaped to find suitable album
		changes = Set.of(AudioDataChange.builder().fileDescriptor(file2).artist("A%").track("my song").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
	}
	
	@Test
	void testFilesWithoutPath() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("01-satisfaction.mp3", "01-satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("02-angie.mp3", "02-angie.mp3");
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).track("satisfaction").trackNo(1).artist(TestUtils.STONES).album("Best Of").medium("CD 1").build(),
				AudioDataChange.builder().fileDescriptor(file2).track("angie").trackNo(2).artist(TestUtils.STONES).album("Best Of").medium("CD 1").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		Set<FileDescriptor> files = new HashSet<>();
		files.add(file1);
		files.add(file2);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
	}
	
	@Test
	void testDifferentFilesSameData() throws Exception {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cd 5/satisfaction.mp3", "satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("d:/music/satisfaction.mp3", "satisfaction.mp3");
		// save file1
		Set<AudioDataChange> changes = Set.of(AudioDataChange.builder().fileDescriptor(file1).track("satisfaction").artist(TestUtils.STONES).medium("CD 1").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		// save file2
		changes = Set.of(AudioDataChange.builder().fileDescriptor(file2).track("satisfaction").artist(TestUtils.STONES).medium("CD 1").build());
		files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		
		Set<FileDescriptor> foundedFiles = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		assertEquals(2, foundedFiles.size());
	}
}
