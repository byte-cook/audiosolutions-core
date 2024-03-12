package de.kobich.audiosolutions.core.service.medium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class MediumServiceTest {
	private static final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	private static final String CD_1 = "CD 1";
	private static final String CD_2 = "CD 2";
	@Autowired
	private AudioSearchService searchService;
	@Autowired
	private MediumService mediumService;
	@Autowired 
	private AudioPersistenceService persistenceService;
	@Autowired 
	private AudioDataService dataService;
	
	@AfterEach
	public void afterEach() throws AudioException {
		persistenceService.removeAll();
	}
	
	@Test
	public void testLendAndReturnMedium() throws AudioException {
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/memory motel.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/start me up.mp3");
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/Beatles/yesterday.mp3");
				
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).medium(CD_1).artist(TestUtils.STONES).track("memory motel").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist(TestUtils.STONES).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file3).medium(CD_2).artist("Beatles").track("yesterday").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		
		MediumResponse res = mediumService.lendMediums(Set.of(CD_1), "Example");
		assertTrue(res.getFailedMediumNames().isEmpty());
		
		List<Medium> mediums = searchService.searchMediums(null);
		Medium cd1 = mediums.stream().filter(m -> CD_1.equals(m.getName())).findAny().orElse(null);
		assertNotNull(cd1);
		assertTrue(cd1.isLent());
		assertEquals("Example", cd1.getBorrower());
		
		res = mediumService.returnMediums(Set.of(CD_1));
		assertTrue(res.getFailedMediumNames().isEmpty());
		mediums = searchService.searchMediums(CD_1);
		assertFalse(mediums.isEmpty());
		cd1 = mediums.iterator().next();
		assertFalse(cd1.isLent());
		assertNull(cd1.getBorrower());
	}

	@Test
	public void testMediumStatistics() throws AudioException {
		Set<FileDescriptor> files = TestUtils.createFileDescriptors(
				"/cd 1/Artist 1/Album 11/t1.mp3", "/cd 1/Artist 1/Album 11/t2.mp3",
				"/cd 1/Artist 1/Album 12/t1.mp3", "/cd 1/Artist 1/Album 12/t2.mp3",
				"/cd 1/Artist 2/Album 21/t1.mp3", "/cd 1/Artist 2/Album 21/t2.mp3",
				"/cd 1/Artist 2/Album 22/t1.mp3", "/cd 1/Artist 2/Album 22/t2.mp3",
				"/cd 2/Artist A/Album A1/t1.mp3", "/cd 2/Artist A/Album A1/t2.mp3",
				"/cd 2/Artist A/Album A2/t1.mp3", "/cd 2/Artist A/Album A2/t2.mp3"
				);
		dataService.addAudioDataByStructure(files, "/<medium>/<artist>/<album>/<track>.<trackFormat>", PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		AudioStatistics stat = persistenceService.getStatistics();
		assertEquals(12, stat.getTrackCount());
		assertEquals(3, stat.getArtistCount());

		// check statistic for both mediums
		List<Medium> mediums = searchService.searchMediums(null);
		stat = mediumService.getStatistics(new HashSet<>(mediums));
		assertEquals(mediums.size(), stat.getMediumCount());
		assertEquals(12, stat.getTrackCount());
		assertEquals(6, stat.getAlbumCount());
		assertEquals(3, stat.getArtistCount());
		assertEquals(1, stat.getGenreCount());

		// check statistic for both mediums
		mediums = searchService.searchMediums("cd 2");
		stat = mediumService.getStatistics(new HashSet<>(mediums));
		assertEquals(mediums.size(), stat.getMediumCount());
		assertEquals(4, stat.getTrackCount());
		assertEquals(2, stat.getAlbumCount());
		assertEquals(1, stat.getArtistCount());
		assertEquals(1, stat.getGenreCount());
	}

}
