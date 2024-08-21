package de.kobich.audiosolutions.core.service.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
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
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.data.AudioDataService;
import de.kobich.audiosolutions.core.service.mp3.id3.FileID3TagServiceTest;
import de.kobich.audiosolutions.core.service.mp3.id3.ID3TagVersion;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.misc.rename.rule.AttributeRenameRule;
import de.kobich.commons.misc.rename.rule.AutoNumberRenameRule;
import de.kobich.commons.misc.rename.rule.CuttingByIndexRenameRule;
import de.kobich.commons.misc.rename.rule.FillRenameRule;
import de.kobich.commons.misc.rename.rule.IRenameRule;
import de.kobich.commons.misc.rename.rule.InsertingByPositionRenameRule;
import de.kobich.commons.misc.rename.rule.RenameFileNameType;
import de.kobich.commons.misc.rename.rule.RenamePositionType;
import de.kobich.commons.misc.rename.rule.SelectingByFileNameTypeRenameRule;
import de.kobich.commons.misc.rename.rule.SelectingByPatternRenameRule;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.SysoutProgressMonitor;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.FileDescriptorService;
import de.kobich.component.file.descriptor.IRenameAttributeProvider;
import de.kobich.component.file.descriptor.RenameFileDescriptor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class FileDescriptorServiceTest {
	private final IServiceProgressMonitor PROGRESS_MONITOR = new SysoutProgressMonitor();
	@Autowired
	private AudioDataService dataService;
	@Autowired
	private FileDescriptorService descriptionService;
	@Autowired
	private AudioPersistenceService persistenceService;
	@Autowired
	private IFileID3TagService id3TagService;
	
	@AfterEach
	public void afterEach() throws AudioException {
		persistenceService.removeAll();
	}

	@Test
	public void testReadFiles() throws Exception {
		final File rootFolder = new File(FileDescriptorServiceTest.class.getResource("/files/").toURI());

		File importFolder1 = new File(rootFolder, "folder1");
		Set<FileDescriptor> files = descriptionService.readFiles(importFolder1, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertEquals(10, files.size());

		// persist + modify directory
		dataService.applyChanges(files.stream().limit(4).collect(Collectors.toSet()), AudioDataChange.builder().genre("Test").build(), PROGRESS_MONITOR);
		persistenceService.persist(files, PROGRESS_MONITOR);
		
		FileFilter fileFilter = new PersistedFileFilter(importFolder1, persistenceService).negate();
		files = descriptionService.readFiles(importFolder1, fileFilter, PROGRESS_MONITOR);
		assertEquals(6, files.size());
	}
	
	@Test
	public void testRenameFilesAutoNumber() throws Exception {
		final File importFolder1 = new File(FileDescriptorServiceTest.class.getResource("/files/folder1").toURI());
		File targetDir = TestUtils.getOutputDir("file-rename-autonumber", true, importFolder1);
		Set<FileDescriptor> files = descriptionService.readFiles(targetDir, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertEquals(10, files.size());
		assertTrue(new File(targetDir, "file1.txt").exists());
		
		Set<RenameFileDescriptor> fileRenameable = files.stream().map(f -> new RenameFileDescriptor(f, null)).collect(Collectors.toSet());
		List<IRenameRule> rules = List.of(new InsertingByPositionRenameRule(RenamePositionType.BEFORE, "-"), new AutoNumberRenameRule(RenamePositionType.BEFORE, 1, 1, 2, false, false));
		descriptionService.renameFiles(fileRenameable, rules, PROGRESS_MONITOR);
		assertTrue(new File(targetDir, "01-file1.txt").exists());
		assertTrue(new File(targetDir, "02-file10.txt").exists());
		assertTrue(new File(targetDir, "03-file2.txt").exists());
		assertTrue(new File(targetDir, "07-file6.txt").exists());
	}
	
	@Test
	public void testRenameFilesFill() throws Exception {
		final File importFolder1 = new File(FileDescriptorServiceTest.class.getResource("/files/folder1").toURI());
		File targetDir = TestUtils.getOutputDir("file-rename-fill", true, importFolder1);
		Set<FileDescriptor> files = descriptionService.readFiles(targetDir, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertTrue(new File(targetDir, "file1.txt").exists());
		
		Set<RenameFileDescriptor> fileRenameable = files.stream().map(f -> new RenameFileDescriptor(f, null)).collect(Collectors.toSet());
		
		final StructureVariable v1 = new StructureVariable("<1>");
		List<IRenameRule> rules = List.of(new SelectingByPatternRenameRule(List.of(v1), v1, "file<1>.txt"), new FillRenameRule(RenamePositionType.BEFORE, '0', 3));
		descriptionService.renameFiles(fileRenameable, rules, PROGRESS_MONITOR);
		assertTrue(new File(targetDir, "file001.txt").exists());
		assertTrue(new File(targetDir, "file002.txt").exists());
		assertTrue(new File(targetDir, "file010.txt").exists());
	}

	@Test
	public void testRenameDoNotOverwrite() throws Exception {
		final File importFolder1 = new File(FileDescriptorServiceTest.class.getResource("/files/folder1").toURI());
		File targetDir = TestUtils.getOutputDir("file-rename-overwrite", true, importFolder1);
		Set<FileDescriptor> files = descriptionService.readFiles(targetDir, WildcardFileFilter.builder().setWildcards("*").get(), PROGRESS_MONITOR);
		assertTrue(new File(targetDir, "file1.txt").exists());
		
		Set<RenameFileDescriptor> fileRenameable = files.stream().map(f -> new RenameFileDescriptor(f, null)).collect(Collectors.toSet());
		List<IRenameRule> rules = List.of(new SelectingByFileNameTypeRenameRule(RenameFileNameType.BASENAME), new CuttingByIndexRenameRule(RenamePositionType.AFTER, 2));
		descriptionService.renameFiles(fileRenameable, rules, PROGRESS_MONITOR);
		// file1.txt -> fil.txt
		assertFalse(new File(targetDir, "file1.txt").exists());
		assertTrue(new File(targetDir, "fil.txt").exists());
		// file10.txt -> file.txt
		assertFalse(new File(targetDir, "file10.txt").exists());
		assertTrue(new File(targetDir, "file.txt").exists());
		// file2.txt -> fil.txt already exists -> no change
		assertTrue(new File(targetDir, "file2.txt").exists());
	}

	@Test
	public void testRenameAttributeByID3() throws Exception {
		File testFile = new File(FileID3TagServiceTest.class.getResource("/mp3/01-mp3-no-tags.mp3").toURI());
		File outputDir = TestUtils.getOutputDir("file-rename-attribute-id3", true);
		File targetFile1 = new File(outputDir, "file1.mp3");
		FileUtils.copyFile(testFile, targetFile1);
		File targetFile2 = new File(outputDir, "file2.mp3");
		FileUtils.copyFile(testFile, targetFile2);

		FileDescriptor fd1 = new FileDescriptor(targetFile1, outputDir);
		FileDescriptor fd2 = new FileDescriptor(targetFile2, outputDir);
		id3TagService.writeSingleID3Tag(Set.of(fd1, fd2), MP3ID3TagType.ARTIST, "Rolling Stones", ID3TagVersion.ALL, null);
		id3TagService.writeSingleID3Tag(Set.of(fd1), MP3ID3TagType.TRACK_NO, "1", ID3TagVersion.ALL, null);
		id3TagService.writeSingleID3Tag(Set.of(fd2), MP3ID3TagType.TRACK_NO, "2", ID3TagVersion.ALL, null);
		
		final IRenameAttributeProvider attributeProvider = new RenameAttributeProvider(id3TagService);
		Set<RenameFileDescriptor> fileRenameable = Set.of(fd1, fd2).stream().map(f -> new RenameFileDescriptor(f, attributeProvider)).collect(Collectors.toSet());
		final String trackNo = RenameFileDescriptorAttributeType.ID3_TRACK_NO.getName();
		final String artist = RenameFileDescriptorAttributeType.ID3_ARTIST.getName();
		List<IRenameRule> rules = List.of(new SelectingByFileNameTypeRenameRule(RenameFileNameType.BASENAME), new AttributeRenameRule(trackNo + "-" + artist, Set.of(trackNo, artist)));
		descriptionService.renameFiles(fileRenameable, rules, PROGRESS_MONITOR);
		assertTrue(new File(outputDir, "01-Rolling Stones.mp3").exists());
		assertTrue(new File(outputDir, "02-Rolling Stones.mp3").exists());
	}

	@Test
	public void testRenameAttributeByAudioData() throws Exception {
		File testFile = new File(FileID3TagServiceTest.class.getResource("/mp3/01-mp3-no-tags.mp3").toURI());
		File outputDir = TestUtils.getOutputDir("file-rename-attribute-audiodata", true);
		File targetFile1 = new File(outputDir, "file1.mp3");
		FileUtils.copyFile(testFile, targetFile1);
		File targetFile2 = new File(outputDir, "file2.mp3");
		FileUtils.copyFile(testFile, targetFile2);

		FileDescriptor fd1 = new FileDescriptor(targetFile1, outputDir);
		FileDescriptor fd2 = new FileDescriptor(targetFile2, outputDir);
		Set<AudioDataChange> changes = Set.of(
				AudioDataChange.builder().fileDescriptor(fd1).trackNo(1).artist("Rolling Stones").build(),
				AudioDataChange.builder().fileDescriptor(fd2).trackNo(2).artist("Rolling Stones").build()
				);
		Set<FileDescriptor> files = dataService.applyChanges(changes, PROGRESS_MONITOR);
		
		final IRenameAttributeProvider attributeProvider = new RenameAttributeProvider(id3TagService);
		Set<RenameFileDescriptor> fileRenameable = files.stream().map(f -> new RenameFileDescriptor(f, attributeProvider)).collect(Collectors.toSet());
		String trackNo = RenameFileDescriptorAttributeType.AUDIO_TRACK_NO.getName();
		String artist = RenameFileDescriptorAttributeType.AUDIO_ARTIST.getName();
		List<IRenameRule> rules = List.of(new SelectingByFileNameTypeRenameRule(RenameFileNameType.BASENAME), new AttributeRenameRule(trackNo + "-" + artist, Set.of(trackNo, artist)));
		descriptionService.renameFiles(fileRenameable, rules, PROGRESS_MONITOR);
		assertTrue(new File(outputDir, "01-Rolling Stones.mp3").exists());
		assertTrue(new File(outputDir, "02-Rolling Stones.mp3").exists());
	}

}
