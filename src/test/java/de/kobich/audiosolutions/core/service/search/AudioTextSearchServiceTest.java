package de.kobich.audiosolutions.core.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioTextSearchServiceTest {
	private static final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	private static final String CD_1 = "CD 1";
	private static final String DSIK_2 = "disk 2";
	@Autowired
	private AudioTextSearchService textSearchService;
	@Autowired
	private AudioSearchService searchService;

	@BeforeAll
	public static void init(@Autowired AudioPersistenceService persistenceService, @Autowired AudioDataService dataService) throws AudioException {
		Set<AudioDataChange> audioFiles = new HashSet<>();
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").medium(CD_1).genre("Rock").artist(TestUtils.STONES).track("memory motel").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Tattoo You/start me up.mp3").artist(TestUtils.STONES).genre("Rock").album("Tattoo You").track("start me up").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Tattoo You/sympathy for the devil.mp3").artist(TestUtils.STONES).album("Tattoo You").track(TestUtils.SYMPATHY_DEVIL).build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/Best of/bargain.mp3").artist("The Who").album("Best of").track("bargain").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Beatles/Best of/yesterday.mp3").medium(DSIK_2).genre("Pop").artist("Beatles").album("Best of").track("yesterday").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/George Thorogood/bad to the bone.mp3").medium(DSIK_2).genre("Rock").artist("George Thorogood").track("bad to the bone").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Nirvana/smells like teen spirit.mp3").medium(DSIK_2).genre("Rock").artist("Nirvana").track("smells like teen spirit").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/ACDC/thunderstruck.mp3").medium(DSIK_2).genre("Rock").artist("ACDC").track("thunderstruck").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/ACDC/back in black.mp3").medium(DSIK_2).genre("Rock").artist("ACDC").track("back in black").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/ACDC/Best of/highway to hell.mp3").medium(DSIK_2).genre("Rock").artist("ACDC").album("Best of").track("highway to hell").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/ABBA/dancing queen.mp3").medium(DSIK_2).genre("Pop").artist("ABBA").track("dancing queen").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Alice Cooper/i'm eighteen.mp3").medium(DSIK_2).genre("Rock").artist("Alice Cooper").track("i'm eighteen").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/B B King/the letter.mp3").medium(DSIK_2).genre("Blues").artist("B B King").track("the letter").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Elvis Presley/too much.mp3").medium(DSIK_2).genre("Rock n Roll").artist("Elvis Presley").track("too much").build());
		audioFiles.add(TestUtils.createAudioDataChangeBuilder("/cdrom/James Brown/try me.mp3").medium(DSIK_2).genre("Funk").artist("James Brown").track("try me").build());
		
		// insert more data for performance test only
//		for (int i = 0; i < 10000; ++i) {
//			audioFiles.add(TestUtils.createAudioDataChangeBuilder("/many/no-" + i).artist("Many").track("track-"+i)));
//		}
		
		Set<FileDescriptor> files = dataService.applyChanges(audioFiles, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		
		assertEquals(15, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(13, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(11, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(6, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(3, persistenceService.getCount(AudioAttribute.MEDIUM));
	}
	
	@AfterAll
	public static void clean(@Autowired AudioPersistenceService persistenceService) throws AudioException {
		persistenceService.removeAll();
	}
	
	@Test
	public void findArtists() throws AudioException {
		List<String> stones = List.of("artist:rolli", "artist: roll", "artist  : ston", "track: start", "stones", "track: sympa", "ston track: sympath", "genre : rock", "genre:rock", "medium: cd");
		for (String input : stones) {
			AudioTextSearchResult res = textSearchService.search(input, 10, PROGRESS_MONITOR);
			assertFalse(res.getArtists().isEmpty(), input);
			assertEquals(TestUtils.STONES, res.getArtists().stream().filter(a -> TestUtils.STONES.equals(a.getName())).findFirst().get().getName(), input);
		}
		assertEquals(11, textSearchService.search("", 11, PROGRESS_MONITOR).getArtists().size());
		
		AudioTextSearchResult res = textSearchService.search("", 10, PROGRESS_MONITOR);
		assertFalse(res.getArtists().isEmpty());
		res.getArtists().forEach(a -> System.out.println(a));
		assertEquals(10, res.getArtists().size());
	}
	
	@Test
	public void findNotArtists() throws AudioException {
		AudioTextSearchResult res = textSearchService.search("-artist:rolli", 10, PROGRESS_MONITOR);
		assertFalse(res.getArtists().isEmpty());
		assertTrue(res.getArtists().stream().filter(a -> TestUtils.STONES.equals(a.getName())).findFirst().isEmpty());

		res = textSearchService.search("-artist: stones track:the", 10, PROGRESS_MONITOR);
		assertEquals(2, res.getArtists().size());
		List<String> artistNames = res.getArtists().stream().map(Artist::getName).toList();
		assertTrue(artistNames.contains("George Thorogood"));
		assertTrue(artistNames.contains("B B King"));
	}
	
	@Test
	public void findAlbums() throws AudioException {
		List<String> stones = List.of("artist:rolli", "artist: roll", "artist  : ston", "tattoo", " \"tattoo you\"", "track: start", "artist: stones", "track: me up", "ston album:tatt", "genre : rock", "genre:rock");
		for (String input : stones) {
			AudioTextSearchResult res = textSearchService.search(input, 10, PROGRESS_MONITOR);
			assertFalse(res.getAlbums().isEmpty(), input);
			List<String> albumNames = res.getAlbums().stream().map(Album::getName).toList();
			assertTrue(albumNames.contains("Tattoo You"), input);
		}
		AudioTextSearchResult res = textSearchService.search("", 10, PROGRESS_MONITOR);
		assertFalse(res.getAlbums().isEmpty());
		assertEquals(10, res.getAlbums().size());
	}
	
	@Test
	public void findNotAlbums() throws AudioException {
		AudioTextSearchResult res = textSearchService.search("stones -album:tattoo", 10, PROGRESS_MONITOR);
		assertFalse(res.getAlbums().isEmpty());
		assertEquals(AudioData.DEFAULT_VALUE, res.getAlbums().get(0).getName());

		res = textSearchService.search("-album: best ACDC", 10, PROGRESS_MONITOR);
		assertEquals(1, res.getAlbums().size());
		assertEquals(AudioData.DEFAULT_VALUE, res.getAlbums().get(0).getName());
	}

	@Test
	public void findTracks() throws AudioException {
		List<String> stones = List.of("artist:rolli", "artist: roll", "artist  : ston", "track: motel", "artist:stones", "mem mo", "ston track:mem", "artist:ston mem", "genre : rock", "genre:rock", "medium: cd");
		for (String input : stones) {
			AudioTextSearchResult res = textSearchService.search(input, 10, PROGRESS_MONITOR);
			assertFalse(res.getTracks().isEmpty(), input);
			List<String> trackNames = res.getTracks().stream().map(Track::getName).toList();
			assertTrue(trackNames.contains("memory motel"), input);
		}
		AudioTextSearchResult res = textSearchService.search("", 10, PROGRESS_MONITOR);
		assertFalse(res.getTracks().isEmpty());
		assertEquals(10, res.getTracks().size());
	}
	
	@Test
	public void findNotTracks() throws AudioException {
		AudioTextSearchResult res = textSearchService.search("stones -track: \"start me up\" -track : symp", 10, PROGRESS_MONITOR);
		assertEquals(1, res.getTracks().size());
		assertEquals("memory motel", res.getTracks().get(0).getName());
	}
	
	@Test
	public void findNotGenre() throws AudioException {
		AudioTextSearchResult res = textSearchService.search("stones -genre:rock", 10, PROGRESS_MONITOR);
		assertEquals(1, res.getTracks().size());
		assertEquals("sympathy for the devil", res.getTracks().get(0).getName());
	}
	
	@Test
	public void findNotMedium() throws AudioException {
		AudioTextSearchResult res = textSearchService.search("stones -medium:1", 10, PROGRESS_MONITOR);
		assertEquals(2, res.getTracks().size());
		List<String> trackNames = res.getTracks().stream().map(Track::getName).toList();
		assertTrue(trackNames.contains("sympathy for the devil"));
		assertTrue(trackNames.contains("start me up"));
	}

	@Test
	public void testOpenResult() throws AudioException {
		List<Album> albums = textSearchService.searchAlbums("artist : beatles best of", 10, PROGRESS_MONITOR);
		assertEquals(1, albums.size());
		
		AudioSearchQuery query = AudioSearchQuery.builder().albumId(albums.get(0).getId()).build();
		Set<FileDescriptor> files = searchService.search(query, PROGRESS_MONITOR);
		TestUtils.printFileDescriptors(files);
		assertEquals(1, files.size());
	}

	@Test
	public void testPerformance() throws AudioException {
		// call once to init caches
		textSearchService.search("roll track: sympat", 10, PROGRESS_MONITOR);
		
		StopWatch watch = new StopWatch();
		watch.start();
		AudioTextSearchResult res = textSearchService.search("roll track: sympat", 10, PROGRESS_MONITOR);
		watch.stop();
		System.out.println("Method search() takes: " + watch.getTime(TimeUnit.MILLISECONDS));
		assertEquals(1, res.getTracks().size());
		watch.reset();
		
		watch.start();
		res = textSearchService.searchSimultaneously("roll track: sympat", 10, PROGRESS_MONITOR);
		watch.stop();
		System.out.println("Method searchSimultaneously() takes: " + watch.getTime(TimeUnit.MILLISECONDS));
		assertEquals(1, res.getTracks().size());
	}
	
	@Test
	public void testParallelPerformance() throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		Set<Thread> threads = new HashSet<>();
		for (int i = 0; i < 3; ++ i) {
			final int index = i;
			Thread t = new Thread(() -> {
				try {
					StopWatch threadWatch = new StopWatch();
					threadWatch.start();
					List<Artist> artists = textSearchService.searchSimultaneously("roll sympa", 20, null).getArtists();
					System.out.println(index + ": " + artists);
					System.out.println("Thread " + index + " takes: " + watch.getTime(TimeUnit.MILLISECONDS));
				}
				catch (AudioException e) {
					e.printStackTrace();
				}
			});
			threads.add(t);
			t.start();
		}
		
		for (Thread t : threads) {
			t.join();
		}
		
		System.out.println("Method testParallelPerformance() takes: " + watch.getTime(TimeUnit.MILLISECONDS));
	}

}
