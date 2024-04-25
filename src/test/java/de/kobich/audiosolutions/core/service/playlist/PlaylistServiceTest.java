package de.kobich.audiosolutions.core.service.playlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioSolutionsTestSpringConfig;
import de.kobich.audiosolutions.core.service.TestUtils;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFileRepository;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFolderRepository;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistRepository;
import de.kobich.component.file.FileResult;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=AudioSolutionsTestSpringConfig.class)
public class PlaylistServiceTest {
	private static final Logger logger = Logger.getLogger(PlaylistServiceTest.class);
	private static File file1;
	private static File file2;
	private static File file3;
	private static File file4;
	private static File file5;
	private static File file6;
	@Autowired
	private PlaylistService playlistService;
	@Autowired
	private PlaylistRepository playlistRepository;
	@Autowired
	private PlaylistFolderRepository playlistFolderRepository;
	@Autowired
	private PlaylistFileRepository playlistFileRepository;
	
	@BeforeAll
	public static void init() throws URISyntaxException {
		final File rootFolder = new File(PlaylistServiceTest.class.getResource("/files/folder1").toURI());
		file1 = new File(rootFolder, "file1.txt");
		file2 = new File(rootFolder, "file2.txt");
		file3 = new File(rootFolder, "file3.txt");
		file4 = new File(rootFolder, "file4.txt");
		file5 = new File(rootFolder, "file5.txt");
		file6 = new File(rootFolder, "file6.txt");
	}
	
	@AfterEach
	public void afterEach() {
		playlistFileRepository.deleteAll();
		playlistFolderRepository.deleteAll();
		playlistRepository.deleteAll();
	}
	
	@Test
	public void testNewPlaylist() throws AudioException {
		EditablePlaylist ep = playlistService.createNewPlaylist("test", false);
		EditablePlaylistFolder ef1 = ep.createOrGetFolder("folder 1");
		ep.addFiles(Set.of(file1), ef1);
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(1, playlistFolderRepository.count());
		assertEquals(1, playlistFileRepository.count());
		
		List<Playlist> list = playlistService.getPlaylists(null);
		assertEquals(1, list.size());
		
		ep = playlistService.openPlaylist(list.get(0), null);
		assertEquals(1, ep.getFolders().size());
		ef1 = ep.getFolders().iterator().next();
		assertEquals("/folder 1", ef1.getPath());
		assertEquals(1, ef1.getFiles().size());
		assertEquals(file1.getName(), ef1.getFiles().iterator().next().getName());
	}
	
	@Test
	public void testEditPlaylist() throws AudioException {
		// create new playlist and save
		EditablePlaylist ep = playlistService.createNewPlaylist("test", false);
		EditablePlaylistFolder ef1 = ep.createOrGetFolder("folder 1");
		ep.addFiles(Set.of(file1), ef1);
		playlistService.savePlaylist(ep, null);
		
		// change and save again
		ep.createOrGetFolder("folder 2");
		ep.addFiles(Set.of(file2, file3), null);
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(3, playlistFolderRepository.count());
		assertEquals(3, playlistFileRepository.count());
		
		// open playlist
		List<Playlist> list = playlistService.getPlaylists(null);
		ep = playlistService.openPlaylist(list.get(0), null);
		// delete folder
		EditablePlaylistFolder f2 = ep.getFolder("folder 2").orElse(null);
		assertNotNull(f2);
		ep.remove(f2);
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(2, playlistFolderRepository.count());
		assertEquals(3, playlistFileRepository.count());
		// delete a file
		EditablePlaylistFolder root = ep.getFolder(EditablePlaylist.ROOT).orElse(null);
		assertNotNull(root);
		assertEquals(2, root.getFiles().size());
		EditablePlaylistFile efile3 = root.getFiles().stream().filter(f -> f.getName().equals(file3.getName())).findFirst().orElse(null);
		assertNotNull(efile3);
		ep.remove(efile3);
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(2, playlistFolderRepository.count());
		assertEquals(2, playlistFileRepository.count());
		// delete folder with files
		ep.remove(root);
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(1, playlistFolderRepository.count());
		assertEquals(1, playlistFileRepository.count());
		ep.removeAll();
		playlistService.savePlaylist(ep, null);
		assertEquals(1, playlistRepository.count());
		assertEquals(0, playlistFolderRepository.count());
		assertEquals(0, playlistFileRepository.count());
	}
	
	@Test
	public void testMoveToFolder() throws AudioException {
		EditablePlaylist ep = playlistService.createNewPlaylist("test", false);
		EditablePlaylistFolder ef1 = ep.createOrGetFolder("folder 1");
		ep.addFiles(Set.of(file1), ef1);
		EditablePlaylistFolder ef2 = ep.createOrGetFolder("folder 2");
		ep.moveToFolder(Set.of(ef1), Set.of(), ef2);
		assertEquals(1, ep.getFolders().size());
		ef1 = ep.getFolders().iterator().next();
		assertEquals(1, ef1.getFiles().size());
		ep.moveToFolder(Set.of(ef2), Set.of(), ef2);
		assertEquals(1, ep.getFolders().size());
		ef1 = ep.getFolders().iterator().next();
		assertEquals(1, ef1.getFiles().size());
	}
	
	private static class TestPropertyChangeListener implements PropertyChangeListener {
		public int count = 0;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			++this.count;
		}
	}
	
	@Test
	public void testPropertyChangeSupport() throws AudioException {
		final TestPropertyChangeListener listener = new TestPropertyChangeListener();
		EditablePlaylist ep = playlistService.createNewPlaylist("test", false);
		ep.getPropertyChangeSupport().addPropertyChangeListener(listener);
		
		ep.setName("dummy");
		assertEquals(1, listener.count);
		EditablePlaylistFolder folder = ep.createOrGetFolder("f2");
		assertEquals(2, listener.count);
		ep.addFiles(Set.of(file1), folder);
		assertEquals(3, listener.count);
	}
	
	@Test
	public void testEditPlaylistName() throws AudioException {
		// create new playlist and save
		EditablePlaylist ep = playlistService.createNewPlaylist("test", false);
		EditablePlaylistFolder ef1 = ep.createOrGetFolder("folder 1");
		ep.addFiles(Set.of(file1), ef1);
		playlistService.savePlaylist(ep, null);
		
		ep.setName("new name");
		playlistService.savePlaylist(ep, null);
		List<Playlist> list = playlistService.getPlaylists(null);
		assertEquals(1, list.size());
		
		ep = playlistService.openPlaylist(list.get(0), null);
		assertEquals(1, ep.getFolders().size());
		assertEquals(1, ep.getFolders().iterator().next().getFiles().size());
		assertEquals("new name", list.iterator().next().getName());
		assertEquals(1, playlistRepository.count());
		assertEquals(1, playlistFolderRepository.count());
		assertEquals(1, playlistFileRepository.count());
	}
	
	@Test
	public void testAppendFilesAfter() throws AudioException {
		EditablePlaylist ep = playlistService.createNewPlaylist("list 1", true);
		// file3 is not in the playlist
		ep.appendFilesAfter(Set.of(file1, file2), new EditablePlaylistFile(file3.getName(), null, file3, 0));
		assertEquals(2, ep.getAllFiles().size());
	}
	
	@Test
	public void testSortOrder() throws AudioException {
		EditablePlaylist ep = playlistService.createNewPlaylist("list 1", true);
		EditablePlaylistFile ef2 = ep.appendFiles(Set.of(file1, file2)).stream().filter(f -> f.getFile().equals(file2)).findFirst().orElse(null);
		assertNotNull(ef2);
		ep.appendFiles(Set.of(file5, file6));
		ep.appendFilesAfter(Set.of(file3, file4), ef2);
		Playlist p = playlistService.savePlaylist(ep, null);
		ep = playlistService.openPlaylist(p, null);
		assertEquals("list 1", ep.getName());
		assertEquals(true, ep.isSystem());
		assertEquals(1, ep.getFolders().size());
		EditablePlaylistFolder ef = ep.getFolders().iterator().next();
		assertEquals(6, ef.getFiles().size());
		List<EditablePlaylistFile> fileList = new ArrayList<>(ef.getFiles());
		fileList.sort(EditablePlaylistFileComparator.INSTANCE);
		assertEquals(file1.getAbsolutePath(), fileList.get(0).getFile().getAbsolutePath());
		assertEquals(file2.getAbsolutePath(), fileList.get(1).getFile().getAbsolutePath());
		assertEquals(file3.getAbsolutePath(), fileList.get(2).getFile().getAbsolutePath());
		assertEquals(file4.getAbsolutePath(), fileList.get(3).getFile().getAbsolutePath());
		assertEquals(file5.getAbsolutePath(), fileList.get(4).getFile().getAbsolutePath());
		assertEquals(file6.getAbsolutePath(), fileList.get(5).getFile().getAbsolutePath());
		
		assertTrue(playlistService.getSystemPlaylist("list 1").isPresent());
	}
	
	@Test
	public void testCopyFilesFromPlaylist() throws AudioException {
		EditablePlaylist source = playlistService.createNewPlaylist("source", false);
		Set<EditablePlaylistFile> eFiles = source.addFiles(Set.of(file1, file2, file3), null);
		EditablePlaylistFolder folder = source.createOrGetFolder("folder");
		eFiles.addAll(source.addFiles(Set.of(file4), folder));
		
		EditablePlaylist target = playlistService.createNewPlaylist("target", false);
		target.copyFiles(eFiles);
		assertEquals(2, target.getFolders().size());
		EditablePlaylistFolder root = target.getFolder(EditablePlaylist.ROOT).orElse(null);
		assertNotNull(root);
		assertEquals(3, root.getFiles().size());
		folder = target.getFolder("folder").orElse(null);
		assertNotNull(folder);
		assertEquals(1, folder.getFiles().size());
		
		playlistService.savePlaylist(source, null);
		playlistService.savePlaylist(target, null);
		assertEquals(8, playlistFileRepository.count());
		assertEquals(4, playlistFolderRepository.count());
		assertEquals(2, playlistRepository.count());
	}
	
	@Test
	public void testCopyFileToDir() throws Exception {
		EditablePlaylist ep = playlistService.createNewPlaylist("list 1", false);
		ep.addFiles(Set.of(file1, file2), null);
		EditablePlaylistFolder f1 = ep.createOrGetFolder("folder1");
		EditablePlaylistFile ef3 = ep.addFiles(Set.of(file3), f1).iterator().next();
		ep.renameFile(ef3, "new-file3.txt");
		
		final File targetDir = TestUtils.getOutputDir("playlist-copyfiletodir", true);
		logger.info("Target dir: " + targetDir);
		Set<EditablePlaylistFile> allFiles = ep.getAllFiles();
		FileResult result = playlistService.copyFilesToDir(allFiles, targetDir, null);
		assertTrue(result.getFailedFiles().isEmpty());
		assertTrue(new File(targetDir, file1.getName()).exists());
		assertTrue(new File(targetDir, file2.getName()).exists());
		File targetF1 = new File(targetDir, f1.getPath());
		assertTrue(new File(targetF1, "new-file3.txt").exists());

		File nonExisting = new File("non-existing.wav");
		ep.addFiles(Set.of(file4, nonExisting), null);

		allFiles = ep.getAllFiles();
		result = playlistService.copyFilesToDir(allFiles, targetDir, null);
		assertEquals(4, result.getFailedFiles().size());
		assertTrue(new File(targetDir, file4.getName()).exists());
	}
	
	@Test
	public void testDeletePlaylists() throws AudioException {
		EditablePlaylist ep1 = playlistService.createNewPlaylist("list 1", false);
		ep1.addFiles(Set.of(file1), null);
		playlistService.savePlaylist(ep1, null);
		
		EditablePlaylist ep2 = playlistService.createNewPlaylist("list 2", false);
		ep2.addFiles(Set.of(file1), null);
		playlistService.savePlaylist(ep2, null);
		
		EditablePlaylist espl = playlistService.createNewPlaylist("<system>", true);
		playlistService.savePlaylist(espl, null);
		
		List<Playlist> list = playlistService.getPlaylists("");
		assertEquals(2, list.size());
		list = playlistService.getPlaylists("1");
		assertEquals(1, list.size());
		
		playlistService.deletePlaylists(list, null);
		assertEquals(1, playlistFileRepository.count());
		assertEquals(1, playlistFolderRepository.count());
		assertEquals(2, playlistRepository.count());
		
		list = playlistService.getPlaylists("1");
		assertEquals(0, list.size());
		
		list = playlistService.getPlaylists(null);
		assertEquals(1, list.size());
		playlistService.deletePlaylists(list, null);
		assertEquals(0, playlistFileRepository.count());
		assertEquals(0, playlistFolderRepository.count());
		assertEquals(1, playlistRepository.count());
		
		Playlist spl = playlistService.getSystemPlaylist("<system>").orElse(null);
		playlistService.deletePlaylists(Set.of(spl), null);
		assertEquals(0, playlistRepository.count());
	}
}
