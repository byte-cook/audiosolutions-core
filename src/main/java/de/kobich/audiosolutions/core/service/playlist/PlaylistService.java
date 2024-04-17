package de.kobich.audiosolutions.core.service.playlist;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFolder;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistRepository;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.commons.utils.SetUtils;
import de.kobich.component.file.FileResult;
import de.kobich.component.file.FileResultBuilder;

@Service
public class PlaylistService {
	private static final Logger logger = Logger.getLogger(PlaylistService.class);
	@Autowired
	private PlaylistRepository playlistRepository;
	
	public EditablePlaylist createNewPlaylist(@Nullable String name, boolean system) {
		return new EditablePlaylist(name, system);
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public EditablePlaylist openPlaylist(Playlist playlist, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Opening playlist...");

		Playlist p = playlistRepository.findEagerById(playlist.getId()).orElse(null);
		if (p == null) {
			throw new AudioException(AudioException.PLAYLIST_NOT_FOUND_ERROR, playlist.getName());
		}
		EditablePlaylist ep = new EditablePlaylist(p);
		progressSupport.monitorEndTask("Playlist opened");
		return ep;
	}
	
	@Transactional(rollbackFor=AudioException.class)
	public Playlist savePlaylist(EditablePlaylist ePlaylist, @Nullable IServiceProgressMonitor monitor) {
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Saving playlist...");

		Playlist playlist = mapToPlaylist(ePlaylist);
		playlist = playlistRepository.save(playlist);
		ePlaylist.setId(playlist.getId());
		progressSupport.monitorEndTask("Playlist saved");
		return playlist;
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public Optional<Playlist> getSystemPlaylist(String name) {
		return playlistRepository.findByNameAndSystem(name, true);
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public List<Playlist> getPlaylists(@Nullable String name) {
		final boolean system = false;
		if (StringUtils.isNotBlank(name)) {
			return playlistRepository.findAllByNameLikeIgnoreCaseAndSystem(SQLUtils.escapeLikeParam(name, true, true), system);
		}
		else {
			return playlistRepository.findAllBySystem(system);
		}
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public FileResult copyFilesToDir(Set<EditablePlaylistFile> allFiles, File targetDir, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		try {
			ProgressSupport progressSupport = new ProgressSupport(monitor);
			progressSupport.monitorBeginTask("Copying files...", allFiles.size());
			
			FileResultBuilder fileResultBuilder = new FileResultBuilder();
			for (EditablePlaylistFile file : allFiles) {
				final File srcFile = file.getFile();
				progressSupport.monitorSubTask("Copying file: " + srcFile.getAbsolutePath(), 1);
				
				File relativePath = new File(file.getFolder().getPath(), file.getName());
				File targetFile = new File(targetDir, relativePath.getPath());
				if (srcFile.exists() && !targetFile.exists()) {
					FileUtils.copyFile(srcFile, targetFile);
					fileResultBuilder.createdFiles.add(targetFile);
				}
				else {
					fileResultBuilder.failedFiles.add(targetFile);
				}
			}
			progressSupport.monitorEndTask("Copying files finished");
			return fileResultBuilder.build();
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new AudioException(AudioException.IO_ERROR);
		}
	}
	
	@Transactional(rollbackFor=AudioException.class)
	public void deletePlaylists(Collection<Playlist> playlists, @Nullable IServiceProgressMonitor monitor) {
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Deleting playlist...");

		playlistRepository.deleteAll(playlists);
		progressSupport.monitorEndTask("Playlist deleted");
	}
	
	private Playlist mapToPlaylist(EditablePlaylist ePlaylist) {
		// playlist
		Playlist playlist;
		Long id = ePlaylist.getId().orElse(null);
		if (id == null) {
			playlist = new Playlist(ePlaylist.getName(), ePlaylist.isSystem());
		}
		else {
			playlist = playlistRepository.findEagerById(id).orElse(new Playlist(ePlaylist.getName(), ePlaylist.isSystem()));
			playlist.setName(ePlaylist.getName());
			playlist.setSystem(ePlaylist.isSystem());
		}
		
		// folder
		Set<PlaylistFolder> newFolders = new HashSet<>();
		for (EditablePlaylistFolder efolder : ePlaylist.getFolders()) {
			PlaylistFolder newFolder = new PlaylistFolder(efolder.getPath());
			PlaylistFolder folder = playlist.getFolders().stream().filter(f -> f.equals(newFolder)).findFirst().orElse(newFolder);
			newFolders.add(folder);
			
			// file
			Set<PlaylistFile> newFiles = new HashSet<>();
			for (EditablePlaylistFile efile : efolder.getFiles()) {
				PlaylistFile newFile = new PlaylistFile(efile.getName(), efile.getFile().getAbsolutePath());
				PlaylistFile file = folder.getFiles().stream().filter(f -> f.equals(newFile)).findFirst().orElse(newFile);
				file.setSortOrder(efile.getSortOrder());
				newFiles.add(file);
			}
			SetUtils.syncSets(newFiles, folder.getFiles());
		}
		SetUtils.syncSets(newFolders, playlist.getFolders());
		
		logger.info("Editable playlist mapped to: " + playlist);
		return playlist;
	}
	
	

}
