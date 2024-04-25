package de.kobich.audiosolutions.core.service.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioSearchServiceTest {
	private static final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	private static final String CD_1 = "CD 1";
	private static final String DSIK_2 = "disk 2";
	@Autowired
	private AudioSearchService searchService;

	@BeforeAll
	public static void init(@Autowired AudioPersistenceService persistenceService, @Autowired AudioDataService dataService) throws AudioException {
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/memory motel.mp3").medium(CD_1).genre("Rock").artist(TestUtils.STONES).track("memory motel").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Tattoo You/start me up.mp3").artist(TestUtils.STONES).album("Tattoo You").track("start me up").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Rolling Stones/Tattoo You/sympathy for the devil.mp3").artist(TestUtils.STONES).album(TestUtils.BEGGARS_BANQUET).track(TestUtils.SYMPATHY_DEVIL).build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/The Who/Best of/bargain.mp3").artist("The Who").album("Best of").track("bargain").build());
		changes.add(TestUtils.createAudioDataChangeBuilder("/cdrom/Beatles/yesterday.mp3").medium(DSIK_2).genre("Pop").artist("Beatles").album("Best of 2").track("yesterday").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		
		assertEquals(5, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(5, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(3, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(3, persistenceService.getCount(AudioAttribute.MEDIUM));
	}
	
	@AfterAll
	public static void clean(@Autowired AudioPersistenceService persistenceService) throws AudioException {
		persistenceService.removeAll();
	}
	
	@Test
	public void findArtists() {
		assertEquals(1, searchService.searchArtists("The*o*").size());
		assertEquals(3, searchService.searchArtists(null).size());
		assertEquals(1, searchService.searchArtists("stones").size());
		List<Artist> es = searchService.searchArtists("es");
		assertEquals(2, es.size());
		assertTrue(es.stream().anyMatch(a -> TestUtils.STONES.equals(a.getName())));
		assertTrue(es.stream().anyMatch(a -> "Beatles".equals(a.getName())));
		List<Artist> allArtists = searchService.searchArtists(null);
		assertEquals(3, allArtists.size());
	}
	
	@Test
	public void findMediums() {
		assertEquals(1, searchService.searchMediums(CD_1).size());
		assertEquals(3, searchService.searchMediums("").size());
		assertEquals(3, searchService.searchMediums(null).size());
		List<Medium> dis = searchService.searchMediums("dis");
		assertEquals(1, dis.size());
		assertTrue(dis.stream().anyMatch(a -> DSIK_2.equals(a.getName())));
	}
	
	@Test
	public void findMediumProposals() {
		List<String> beg = searchService.searchProposals(AudioAttribute.MEDIUM, "cd", 5);
		assertFalse(beg.isEmpty());
		assertTrue(beg.stream().anyMatch(a -> CD_1.equals(a)));
		assertTrue(searchService.searchProposals(AudioAttribute.MEDIUM, "xx", 5).isEmpty());
	}
	
	@Test
	public void findArtistProposals() {
		List<String> roll = searchService.searchProposals(AudioAttribute.ARTIST, "roll", 5);
		assertFalse(roll.isEmpty());
		assertTrue(roll.stream().anyMatch(a -> TestUtils.STONES.equals(a)));
		List<String> es = searchService.searchProposals(AudioAttribute.ARTIST, "es", 5);
		assertEquals(2, es.size());
		assertEquals("Beatles", es.get(0));
		assertEquals(TestUtils.STONES, es.get(1));
		assertTrue(searchService.searchProposals(AudioAttribute.ARTIST, "xx", 5).isEmpty());
	}

	@Test
	public void findAlbumProposals() {
		List<String> beg = searchService.searchProposals(AudioAttribute.ALBUM, "beg", 5);
		assertFalse(beg.isEmpty());
		assertTrue(beg.stream().anyMatch(a -> TestUtils.BEGGARS_BANQUET.equals(a)));
		assertEquals(2, searchService.searchProposals(AudioAttribute.ALBUM, "of", 5).size());
		assertTrue(searchService.searchProposals(AudioAttribute.ALBUM, "xx", 5).isEmpty());
	}

	@Test
	public void findGenreProposals() {
		List<String> beg = searchService.searchProposals(AudioAttribute.GENRE, "ro", 5);
		assertFalse(beg.isEmpty());
		assertTrue(beg.stream().anyMatch(a -> "Rock".equals(a)));
		assertTrue(searchService.searchProposals(AudioAttribute.GENRE, "xx", 5).isEmpty());
	}

	@Test
	public void findByMediums() {
		Set<FileDescriptor> mediums = searchService.searchByMediums(Set.of(DSIK_2, CD_1), PROGRESS_MONITOR);
		assertEquals(2, mediums.size());
	}

	@Test
	public void findByArtists() {
		Set<FileDescriptor> stones = searchService.searchByArtists(Set.of(TestUtils.STONES), PROGRESS_MONITOR);
		assertEquals(3, stones.size());
	}

	@Test
	public void findByText() throws AudioException {
		Set<FileDescriptor> stones = searchService.searchByText("stones", AudioAttribute.ARTIST, PROGRESS_MONITOR);
		assertEquals(3, stones.size());
		
		Set<FileDescriptor> best = searchService.searchByText("Best", AudioAttribute.ALBUM, PROGRESS_MONITOR);
		assertEquals(2, best.size());

		Set<FileDescriptor> me = searchService.searchByText("me", AudioAttribute.TRACK, PROGRESS_MONITOR);
		assertEquals(2, me.size());
	}
	
	@Test
	public void find() {
		Set<FileDescriptor> rolli = searchService.search(AudioSearchQuery.builder().artistName("rolli").build(), PROGRESS_MONITOR);
		TestUtils.printFileDescriptors(rolli);
		assertEquals(3, rolli.size());

		Set<FileDescriptor> cdAndRolli = searchService.search(AudioSearchQuery.builder().artistName("rolli").mediumName("cd").build(), PROGRESS_MONITOR);
		TestUtils.printFileDescriptors(cdAndRolli);
		assertEquals(1, cdAndRolli.size());
		
		Set<FileDescriptor> yest = searchService.search(AudioSearchQuery.builder().trackName("yest").build(), PROGRESS_MONITOR);
		assertEquals(1, yest.size());

		Set<FileDescriptor> stones = searchService.search(AudioSearchQuery.builder().artistName(TestUtils.STONES).build(), PROGRESS_MONITOR);
		assertEquals(3, stones.size());
		
		Set<FileDescriptor> all = searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		assertEquals(5, all.size());
	}

}
