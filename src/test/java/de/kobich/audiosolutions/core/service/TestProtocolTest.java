package de.kobich.audiosolutions.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.audiosolutions.core.service.search.AudioSearchQuery;
import de.kobich.audiosolutions.core.service.search.AudioSearchService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class TestProtocolTest {
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioDataService dataService;
	@Autowired
	private AudioPersistenceService persistenceService;
	@Autowired
	private AudioSearchService searchService;
	
	@BeforeAll
	@AfterAll
	public static void clean(@Autowired AudioPersistenceService persistenceService) throws AudioException {
		persistenceService.removeAll();
	}
	
	@Test
	public void test() throws AudioException {
		// insert album
		FileDescriptor file1 = TestUtils.createFileDescriptor("01-satisfaction.mp3", "01-satisfaction.mp3");
		FileDescriptor file2 = TestUtils.createFileDescriptor("02-angie.mp3", "02-angie.mp3");
		Set<AudioDataChange> changes = new HashSet<>();
		changes.add(AudioDataChange.builder().fileDescriptor(file1).track("satisfaction").trackNo(1).artist(TestUtils.STONES).album("Best Of").medium("CD 1").build());
		changes.add(AudioDataChange.builder().fileDescriptor(file2).track("angie").trackNo(2).artist(TestUtils.STONES).album("Best Of").medium("CD 1").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		Set<FileDescriptor> files = new HashSet<>();
		files.add(file1);
		files.add(file2);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// insert duplicate file
		files.clear();
		files.add(TestUtils.createFileDescriptor("01-satisfaction.mp3", AudioDataBuilder.builder().track("satisfaction").trackNo(1).artist(TestUtils.STONES).album("Best Of").medium("CD 1")));
		assertThrows(AudioException.class, () -> persistenceService.persist(files, PROGRESS_MONITOR));

		// insert duplicate file + new file
		files.clear();
		files.add(TestUtils.createFileDescriptor("01-satisfaction.mp3", AudioDataBuilder.builder().track("satisfaction").trackNo(1).artist(TestUtils.STONES).album("Best Of").medium("CD 1")));
		files.add(TestUtils.createFileDescriptor("03-memories.mp3", AudioDataBuilder.builder().track("memories").trackNo(3).artist(TestUtils.STONES).album("Best Of").medium("CD 1")));
		assertThrows(AudioException.class, () -> persistenceService.persist(files, PROGRESS_MONITOR));
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));

		// insert new file with same audio data
		FileDescriptor file3 = TestUtils.createFileDescriptor("03-memories.mp3", "03-memories.mp3");
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).track("satisfaction").trackNo(1).artist(TestUtils.STONES).album("Best Of").medium("CD 1").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));

		// change audio data
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).track("memories").trackNo(3).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// change album
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).album("New Album").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));

		// revert change album
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).album("Best Of").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		TestUtils.printFileDescriptors(searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR));
		
		// change artist
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).artist("David Bowie").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));

		// change medium
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).medium("CD 5").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(2, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// revert change artist + medium
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).artist(TestUtils.STONES).medium("CD 1").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// change genre
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).genre("Rock").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		TestUtils.printFileDescriptors(searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR));
		
		// change album publication of file2
		changes.clear();
		final Date albumPublication = new GregorianCalendar(1981, Calendar.MARCH, 12).getTime();
		changes.add(AudioDataChange.builder().fileDescriptor(file2).albumPublication(albumPublication).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file2);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		// --> all files have this publication set 
		Set<FileDescriptor> searchFiles = this.searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		assertEquals(3, searchFiles.size());
		searchFiles.forEach(fd -> assertEquals(albumPublication, fd.getMetaData(AudioData.class).getAlbumPublication().get()));

		// revert change album publication of file3
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file3).albumPublicationRemove(true).build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		// --> no files have this publication set
		searchFiles = this.searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR);
		assertEquals(3, searchFiles.size());
		searchFiles.forEach(fd -> assertFalse(fd.getMetaData(AudioData.class).getAlbumPublication().isPresent()));
		
		// change artist + album
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file2).artist("David Bowie").build());
		changes.add(AudioDataChange.builder().fileDescriptor(file3).artist("David Bowie").album("His Best").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file2);
		files.add(file3);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// add new files
		FileDescriptor file4 = TestUtils.createFileDescriptor("01-who are you.mp3", "01-who are you.mp3");
		FileDescriptor file5 = TestUtils.createFileDescriptor("02-boris the spider.mp3", "02-boris the spider.mp3");
		changes.clear();
		changes.add(AudioDataChange.builder().fileDescriptor(file4).track("who are you").trackNo(1).artist("The Who").album("Mix").medium("CD 2").build());
		changes.add(AudioDataChange.builder().fileDescriptor(file5).track("boris the spider").trackNo(2).artist("The Who").album("Mix").medium("CD 2").build());
		dataService.applyChanges(changes, PROGRESS_MONITOR);
		files.clear();
		files.add(file4);
		files.add(file5);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(5, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(3, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(3, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(2, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// remove file1
		files.clear();
		files.add(file1);
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(4, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(3, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(2, persistenceService.getCount(AudioAttribute.MEDIUM));
		TestUtils.printFileDescriptors(searchService.search(AudioSearchQuery.builder().build(), PROGRESS_MONITOR));
		
		// remove file2
		files.clear();
		files.add(file2);
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(3, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(2, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(2, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(2, persistenceService.getCount(AudioAttribute.MEDIUM));
		
		// remove file3
		files.clear();
		files.add(file3);
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(2, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(1, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(1, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(1, persistenceService.getCount(AudioAttribute.MEDIUM));

		// remove all
		files.clear();
		files.add(file4);
		files.add(file5);
		dataService.removeAudioData(files, PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		assertEquals(0, persistenceService.getCount(AudioAttribute.TRACK));
		assertEquals(0, persistenceService.getCount(AudioAttribute.ARTIST));
		assertEquals(0, persistenceService.getCount(AudioAttribute.ALBUM));
		assertEquals(0, persistenceService.getCount(AudioAttribute.GENRE));
		assertEquals(0, persistenceService.getCount(AudioAttribute.MEDIUM));
	}

}
