package de.kobich.audiosolutions.core.service.cddb;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.cddb.AudioCDDBService.SearchDepth;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioCDDBServiceTest {
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioCDDBService cddbService;
	
	@Test
	public void test() throws AudioException {
		List<ICDDBRelease> releases = this.cddbService.search(TestUtils.STONES, TestUtils.BEGGARS_BANQUET, SearchDepth.SINGLE, null, PROGRESS_MONITOR);
		assertFalse(releases.isEmpty());
		List<ICDDBRelease> release = releases.stream().filter(r -> r.getArtist().contains(TestUtils.STONES) && TestUtils.BEGGARS_BANQUET.equals(r.getAlbum())).toList();
		assertFalse(release.isEmpty());
		boolean songFound = false;
		for (ICDDBRelease r : release) {
			assertTrue(r.getTrackCount() > 0);
			if (!r.isTrackLoaded()) {
				r.loadTracks(new ProgressSupport(PROGRESS_MONITOR));
			}
			
			if (TestUtils.SYMPATHY_DEVIL.equalsIgnoreCase(r.getTrack(0).getName())) {
				songFound = true;
				break;
			}
		}
		assertTrue(songFound);
	}

}
