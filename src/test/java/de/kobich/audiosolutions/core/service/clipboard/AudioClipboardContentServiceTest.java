package de.kobich.audiosolutions.core.service.clipboard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class AudioClipboardContentServiceTest {
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioClipboardContentService contentService;
	@Autowired 
	private AudioDataService dataService;
	
	@Test
	public void testClipboardContent() throws AudioException {
		final Date albumPublication = new GregorianCalendar(1981, Calendar.MARCH, 12).getTime();
		FileDescriptor file1 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/memory motel.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("/cdrom/Rolling Stones/start me up.mp3");
		FileDescriptor file3 = TestUtils.createFileDescriptor("/cdrom/file3.mp3");
		FileDescriptor file4 = TestUtils.createFileDescriptor("/cdrom/Beatles/yesterday.mp3");
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(file1).medium("cd1").artist(TestUtils.STONES).album("Best of").albumPublication(albumPublication).track("start me up").build(),
				AudioDataChange.builder().fileDescriptor(file2).artist(TestUtils.STONES).album("Best of").disk("CD 1").track("memory motel").build(),
				AudioDataChange.builder().fileDescriptor(file3).build(),
				AudioDataChange.builder().fileDescriptor(file4).artist("Beatles").track("yesterday").build());
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		
		String track = contentService.getClipboardContent(files, AudioClipboardContentType.TRACK);
		assertEquals(getExpectedContent("memory motel","start me up","yesterday"), track);

		String album = contentService.getClipboardContent(files, AudioClipboardContentType.ALBUM);
		assertEquals(getExpectedContent("Best of"), album);
		String albumAndPublication = contentService.getClipboardContent(files, AudioClipboardContentType.ALBUM_AND_PUBLICATION);
		assertEquals(getExpectedContent("Best of", "Best of (1981-03-12)"), albumAndPublication);
		String albumAndDisk = contentService.getClipboardContent(files, AudioClipboardContentType.ALBUM_AND_DISK);
		assertEquals(getExpectedContent("Best of", "Best of (CD 1)"), albumAndDisk);

		String artist = contentService.getClipboardContent(files, AudioClipboardContentType.ARTIST);
		assertEquals(getExpectedContent("Beatles", TestUtils.STONES), artist);
		
		String medium = contentService.getClipboardContent(files, AudioClipboardContentType.MEDIUM);
		assertEquals(getExpectedContent("cd1"), medium);
		
		String relativePath = contentService.getClipboardContent(files, AudioClipboardContentType.RELATIVE_PATH);
		assertEquals(getExpectedContent(file4.getRelativePath(), file1.getRelativePath(), file2.getRelativePath(), file3.getRelativePath()), relativePath);
	}
	
	private String getExpectedContent(String... s) {
		return String.join(NEW_LINE, s);
	}

}
