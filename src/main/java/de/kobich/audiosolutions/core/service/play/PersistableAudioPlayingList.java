package de.kobich.audiosolutions.core.service.play;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import de.kobich.audiosolutions.core.service.playlist.EditablePlaylist;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFileComparator;
import de.kobich.audiosolutions.core.service.playlist.EditablePlaylistFolder;
import de.kobich.audiosolutions.core.service.playlist.PlaylistService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The list of files to play.
 * The files in the list can be saved in the database.
 * @see PlaylistService
 */
@RequiredArgsConstructor
public class PersistableAudioPlayingList implements IAudioPlayinglist {
	@Getter
	private final EditablePlaylist playlist;
	@Nullable
	private EditablePlaylistFile currentFile;
	@Setter
	@Getter
	private boolean loopEnabled;
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return this.playlist.getPropertyChangeSupport();
	}
	
	/**
	 * Returns all files in the root folder
	 */
	public synchronized Set<EditablePlaylistFile> getFiles() {
		EditablePlaylistFolder rootFolder = playlist.getFolder(EditablePlaylist.ROOT).orElse(null);
		if (rootFolder == null) {
			return Set.of();
		}
		return rootFolder.getFiles();
	}
	
	/**
	 * Returns all files in the root folder as sorted list
	 * @return
	 */
	public synchronized List<EditablePlaylistFile> getFilesSorted() {
		Set<EditablePlaylistFile> allFiles = getFiles();
		List<EditablePlaylistFile> files = new ArrayList<>(allFiles);
		files.sort(EditablePlaylistFileComparator.INSTANCE);
		return files;
	}
	
	public synchronized boolean isEmpty() {
		return getFiles().isEmpty();
	}
	
	public synchronized Optional<EditablePlaylistFile> getFile(File file) {
		Set<EditablePlaylistFile> files = getFiles();
		for (EditablePlaylistFile f : files) {
			if (f.getFile().equals(file)) {
				return Optional.of(f);
			}
		}
		return Optional.empty();
	}
	
	public synchronized Set<EditablePlaylistFile> appendFiles(Set<File> files) {
		return playlist.appendFiles(files);
	}
	
	public synchronized Set<EditablePlaylistFile> appendFilesAfterCurrent(Set<File> files) {
		if (currentFile == null) {
			return appendFiles(files);
		}
		else {
			return playlist.appendFilesAfter(files, currentFile);
		}
	}
	
	public synchronized boolean removeFiles(Iterable<EditablePlaylistFile> files) {
		boolean modified = false;
		for (EditablePlaylistFile file : files) {
			modified |= playlist.remove(file);
		}
		return modified;
	}
	
	public synchronized boolean removeAll() {
		return playlist.removeAll();
	}
	
	public synchronized void setStartFile(@Nullable EditablePlaylistFile file) {
		this.currentFile = file;
	}
	
	/**
	 * Returns the current file. If this is not set, the first file in the list is returned.
	 */
	@Override
	public synchronized Optional<File> getStartFile() {
		return getCurrentFile().or(() -> {
			List<EditablePlaylistFile> files = getFilesSorted();
			if (!files.isEmpty()) {
				currentFile = files.get(0);
				return Optional.of(currentFile.getFile());
			}
			return Optional.empty();
		});
	}

	public synchronized Optional<File> getCurrentFile() {
		if (currentFile != null) {
			return Optional.ofNullable(currentFile.getFile());
		}
		return Optional.empty();
	}

	@Override
	public synchronized Optional<File> getNextFile() {
		goToNext();
		return getCurrentFile();
	}

	@Override
	public synchronized Optional<File> getPreviousFile() {
		goToPrevious();
		return getCurrentFile();
	}

	private void goToNext() {
		List<EditablePlaylistFile> files = getFilesSorted();
		if (files.isEmpty()) {
			this.currentFile = null;
		}
		else if (this.currentFile == null) {
			this.currentFile = files.get(0);
		}
		else {
			int index = files.indexOf(this.currentFile);
			if (index == -1) {
				// currentFile cannot be found anymore
				index = 0;
			}
			else if (loopEnabled) {
				index = (index + 1) % files.size();
			}
			else { 
				++index;
			}
			setCurrentFileByIndex(files, index);
		}
	}
	
	private void goToPrevious() {
		List<EditablePlaylistFile> files = getFilesSorted();
		if (files.isEmpty()) {
			this.currentFile = null;
		}
		else if (this.currentFile == null) {
			this.currentFile = files.get(0);
		}
		else {
			int index = files.indexOf(this.currentFile);
			if (index == -1) {
				// currentFile cannot be found anymore
				index = 0;
			}
			else if (loopEnabled) {
				if (index == 0) {
					index = files.size() - 1;
				}
				else {
					index = (index - 1) % files.size();
				}
			}
			else {
				-- index;
			}
			setCurrentFileByIndex(files, index);
		}
	}
		
	private void setCurrentFileByIndex(List<EditablePlaylistFile> files, int index) {
		if (0 <= index && index < files.size()) {
			this.currentFile = files.get(index);
		}
		else {
			this.currentFile = null;
		}		
	}

}
