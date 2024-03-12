package de.kobich.audiosolutions.core.service.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.mp3.id3.FileID3TagServiceTest;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class FileDescriptorServiceTest {
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioDataService dataService;
	@Autowired
	private FileDescriptorService fileService;
	@Autowired
	private AudioPersistenceService persistenceService;
	
	@AfterEach
	public void afterEach() throws AudioException {
		persistenceService.removeAll();
	}

	@Test
	public void testReadFiles() throws Exception {
		final File rootFolder = new File(FileID3TagServiceTest.class.getResource("/files/").toURI());

		File importFolder1 = new File(rootFolder, "folder1");
		Set<FileDescriptor> files = fileService.readFiles(importFolder1, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertEquals(10, files.size());

		// persist + modify directory
		dataService.applyChanges(files.stream().limit(4).collect(Collectors.toSet()), AudioDataChange.builder().genre("Test").build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		
		FileFilter fileFilter = new PersistedFileFilter(importFolder1, persistenceService).negate();
		files = fileService.readFiles(importFolder1, fileFilter, PROGRESS_MONITOR);
		assertEquals(6, files.size());
	}
}
