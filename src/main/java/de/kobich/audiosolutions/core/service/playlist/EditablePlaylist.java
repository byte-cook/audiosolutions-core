package de.kobich.audiosolutions.core.service.playlist;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;

import de.kobich.audiosolutions.core.service.playlist.repository.Playlist;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFile;
import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFolder;
import de.kobich.commons.utils.RelativePathUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Editable Playlist. Changes to this playlist are only possible by this class.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class EditablePlaylist {
	public static final String PROP_ADD = "add";
	public static final String PROP_MODIFIED = "modified";
	public static final String PROP_REMOVE = "remove";
	public static final String ROOT = "/";
	
	@Nullable
	@Setter(value = AccessLevel.PROTECTED)
	private Long id;
	
	@EqualsAndHashCode.Include
	@ToString.Include
	@Getter
	private String name;
	@EqualsAndHashCode.Include
	@Getter
	private final boolean system;
	private final Set<EditablePlaylistFolder> folders;
	@Getter
	private final PropertyChangeSupport propertyChangeSupport;
	
	protected EditablePlaylist(String name, boolean system) {
		this.id = null;
		this.name = name;
		this.system = system;
		this.folders = new HashSet<>();
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	protected EditablePlaylist(Playlist playlist) {
		this.id = playlist.getId();
		this.name = playlist.getName();
		this.system = playlist.isSystem();
		this.folders = new HashSet<>();
		for (PlaylistFolder folder : playlist.getFolders()) {
			EditablePlaylistFolder efolder = new EditablePlaylistFolder(folder.getPath());
			this.folders.add(efolder);
			
			for (PlaylistFile file : folder.getFiles()) {
				EditablePlaylistFile efile = new EditablePlaylistFile(file.getName(), efolder, new File(file.getFilePath()), EditablePlaylistFile.DEFAULT_SORT_ORDER);
				efolder.getModifiableFiles().add(efile);
			}
		}
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}
	
	public Optional<Long> getId() {
		return Optional.ofNullable(this.id);
	}
	
	public void setName(String name) {
		propertyChangeSupport.firePropertyChange(PROP_MODIFIED, this.name, name);
		this.name = name;
	}
	
	public Set<EditablePlaylistFolder> getFolders() {
		return Collections.unmodifiableSet(this.folders);
	}
	
	/**
	 * Adds files to the given folder or to ROOT folder
	 * @param files
	 * @param targetFolder
	 * @return the added files
	 */
	public Set<EditablePlaylistFile> addFiles(Set<File> files, @Nullable EditablePlaylistFolder targetFolder) {
		final EditablePlaylistFolder folder = targetFolder != null ? targetFolder : createOrGetFolder(ROOT);
		return addFilesToFolder(files, folder, EditablePlaylistFile.DEFAULT_SORT_ORDER);
	}
	
	/**
	 * Appends files to the end of the playlist. If a file to be appended is already in the list, this file is moved to the end of the list.
	 * @param files
	 * @return
	 */
	public Set<EditablePlaylistFile> appendFiles(Set<File> files) {
		EditablePlaylistFolder rootFolder = createOrGetFolder(ROOT);
		// remove files that already are in the list
		files.forEach(f -> remove(new EditablePlaylistFile(f.getName(), rootFolder, f, EditablePlaylistFile.DEFAULT_SORT_ORDER)));
		// find max. sort order
		long sortOrder = rootFolder.getFiles().stream().mapToLong(EditablePlaylistFile::getSortOrder).max().orElse(EditablePlaylistFile.DEFAULT_SORT_ORDER);
		return addFilesToFolder(files, rootFolder, sortOrder + 1);
	}
	
	/**
	 * Appends files after the given <code>afterFile</code>. If a file to be appended is already in the list, this file is also moved.
	 * @param files
	 * @param afterFile
	 * @return
	 */
	public Set<EditablePlaylistFile> appendFilesAfter(Set<File> files, EditablePlaylistFile afterFile) {
		EditablePlaylistFolder rootFolder = createOrGetFolder(ROOT);
		// remove files that already are in the list
		files.stream().filter(f -> !f.equals(afterFile.getFile())).forEach(f -> remove(new EditablePlaylistFile(f.getName(), rootFolder, f, EditablePlaylistFile.DEFAULT_SORT_ORDER)));
		
		// sort all files in root directory
		List<EditablePlaylistFile> rootFiles = new ArrayList<>(rootFolder.getFiles());
		rootFiles.sort(EditablePlaylistFileComparator.INSTANCE);
		
		// increase sort order of all files after the given file
		Long sortOrder = null; 
		for (EditablePlaylistFile f : rootFiles) {
			if (f.equals(afterFile)) {
				sortOrder = f.getSortOrder();
				// ignore given file
				continue;
			}
			
			// apply only for files after the given file
			if (sortOrder != null) {
				// increase by 2
				f.setSortOrder(f.getSortOrder() + 2);
			}
		}
		if (sortOrder == null) {
			sortOrder = EditablePlaylistFile.DEFAULT_SORT_ORDER;
		}
		
		// increase by 1
		return addFilesToFolder(files, rootFolder, afterFile.getSortOrder() + 1);
	}
	
	private Set<EditablePlaylistFile> addFilesToFolder(Set<File> files, EditablePlaylistFolder folder, long sortOrder) {
		Set<EditablePlaylistFile> newFiles = new HashSet<>();
		for (File file : files) {
			EditablePlaylistFile pf = new EditablePlaylistFile(file.getName(), folder, file.getAbsoluteFile(), sortOrder);
			newFiles.add(pf);
		}
		folder.getModifiableFiles().addAll(newFiles);
		propertyChangeSupport.firePropertyChange(PROP_ADD, null, newFiles);
		return newFiles;
	}
	
	/**
	 * Copies files from another playlist to this one
	 */
	public Set<EditablePlaylistFile> copyFiles(Set<EditablePlaylistFile> files) {
		Set<EditablePlaylistFile> newFiles = new HashSet<>();
		for (EditablePlaylistFile file : files) {
			EditablePlaylistFolder newFolder = createOrGetFolder(file.getFolder().getPath());
			EditablePlaylistFile newFile = new EditablePlaylistFile(file.getName(), newFolder, file.getFile(), EditablePlaylistFile.DEFAULT_SORT_ORDER);
			newFolder.getModifiableFiles().add(newFile);
			propertyChangeSupport.firePropertyChange(PROP_ADD, null, file);
			newFiles.add(newFile);
		}
		return newFiles;
	}

	/**
	 * Creates a new folder or returns an existing one
	 * @return the new folder or an existing one if there is already a folder with this name 
	 */
	public EditablePlaylistFolder createOrGetFolder(String relativePath) {
		EditablePlaylistFolder folder = new EditablePlaylistFolder(relativePath);
		boolean added = folders.add(folder);
		if (added) {
			propertyChangeSupport.firePropertyChange(PROP_MODIFIED, null, folder);
		}
		else {
			folder = getFolder(folder.getPath()).orElseThrow();
		}
		return folder;
	}

	public Set<EditablePlaylistFile> moveToFolder(Collection<EditablePlaylistFolder> sourceFolders, Collection<EditablePlaylistFile> sourceFiles, final EditablePlaylistFolder targetFolder) {
		EditablePlaylistFolder moveToFolder = createOrGetFolder(targetFolder.getPath());
		
		Set<EditablePlaylistFile> newFiles = new HashSet<>();
		Set<EditablePlaylistFile> files2Move = new HashSet<>();
		
		// add and remove selectedFolders 
		for (EditablePlaylistFolder folder : sourceFolders) {
			if (!folder.equals(targetFolder)) {
				remove(folder);	
			}
			files2Move.addAll(folder.getFiles());
		}
		// add selectedFiles
		files2Move.addAll(sourceFiles);
		
		// move to target folder
		for (EditablePlaylistFile file : files2Move) {
			// remove existing file
			remove(file);
			// create new file in target folder
			EditablePlaylistFile newFile = new EditablePlaylistFile(file.getName(), moveToFolder, file.getFile(), file.getSortOrder());
			moveToFolder.getModifiableFiles().add(newFile);
			newFiles.add(newFile);
		}
		propertyChangeSupport.firePropertyChange(PROP_MODIFIED, null, moveToFolder);
		return newFiles;
	}
	
	public Optional<EditablePlaylistFile> renameFile(EditablePlaylistFile oldFile, String newFileName) {
		if (oldFile.getName().equals(newFileName)) {
			return Optional.empty();
		}
		
		EditablePlaylistFile.normalizeAndValidateName(newFileName);
		EditablePlaylistFolder folder = oldFile.getFolder();
		if (folder.getModifiableFiles().remove(oldFile)) {
			EditablePlaylistFile newFile = new EditablePlaylistFile(newFileName, folder, oldFile.getFile(), oldFile.getSortOrder());
			folder.getModifiableFiles().add(newFile);
			propertyChangeSupport.firePropertyChange(PROP_MODIFIED, oldFile, newFile);
			return Optional.of(newFile);
		}
		return Optional.empty();
	}
	
	public Optional<EditablePlaylistFolder> renameFolder(final EditablePlaylistFolder oldFolder, final String newFolderPath) {
		final String normFolderPath = EditablePlaylistFolder.normalizeAndValidatePath(newFolderPath);
		if (oldFolder.getPath().equals(normFolderPath)) {
			return Optional.empty();
		}
		
		if (this.remove(oldFolder)) {
			EditablePlaylistFolder newFolder = createOrGetFolder(normFolderPath);
			moveToFolder(List.of(), oldFolder.getFiles(), newFolder);
			return Optional.of(newFolder);
		}
		return Optional.empty();
	}
	
	public boolean remove(EditablePlaylistFolder folder) {
		if (this.folders.remove(folder)) {
			propertyChangeSupport.firePropertyChange(PROP_REMOVE, folder, null);
			return true;
		}
		return false;
	}
	
	public boolean remove(EditablePlaylistFile file) {
		EditablePlaylistFolder folder = file.getFolder();
		if (folder.getModifiableFiles().remove(file)) {
			propertyChangeSupport.firePropertyChange(PROP_REMOVE, file, null);
			return true;
		}
		return false;
	}
	
	public boolean removeAll() {
		if (!this.folders.isEmpty()) {
			this.folders.clear();
			propertyChangeSupport.firePropertyChange(PROP_REMOVE, this.folders, null);
			return true;
		}
		return false;
	}
	
	public Set<EditablePlaylistFile> getAllFiles() {
		Set<EditablePlaylistFile> allFiles = new HashSet<>();
		getFolders().forEach(f -> allFiles.addAll(f.getFiles()));
		return allFiles;
	}
	
	public Set<EditablePlaylistFolder> getAvaiableFolders(boolean includeRoot) {
		Set<EditablePlaylistFolder> folders = new HashSet<>(this.folders);
		if (includeRoot) {
			folders.add(new EditablePlaylistFolder(ROOT));
		}
		return Collections.unmodifiableSet(folders);
	}
	
	public Set<String> getFolderNameProposals(File file) {
		File parentFile = file.getParentFile();
		String currentRelPath = "";
		
		Set<String> proposals = new HashSet<>();
		for (int i = 0; i < 5; ++i) {
			if (parentFile == null) {
				break;
			}
			currentRelPath = parentFile.getName() + currentRelPath; 
			currentRelPath = RelativePathUtils.convertBackslashToSlash(currentRelPath);
			currentRelPath = RelativePathUtils.ensureStartingSlash(currentRelPath);
			proposals.add(currentRelPath);
			parentFile = parentFile.getParentFile();
		}
		return proposals;
	}
	
	public Optional<EditablePlaylistFolder> getFolder(String relativePath) {
		String normPath = EditablePlaylistFolder.normalizeAndValidatePath(relativePath);
		return folders.stream().filter(f -> f.getPath().equalsIgnoreCase(normPath)).findFirst();
	}
	
}
