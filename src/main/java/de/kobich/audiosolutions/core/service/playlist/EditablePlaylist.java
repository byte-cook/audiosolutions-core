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
@Getter
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
	private String name;
	@EqualsAndHashCode.Include
	private final boolean system;
	private final Set<EditablePlaylistFolder> folders;
	private final PropertyChangeSupport support;
	
	protected EditablePlaylist(String name, boolean system) {
		this.id = null;
		this.name = name;
		this.system = system;
		this.folders = new HashSet<>();
		this.support = new PropertyChangeSupport(this);
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
				efolder.getFiles().add(efile);
			}
		}
		this.support = new PropertyChangeSupport(this);
	}
	
	public Optional<Long> getId() {
		return Optional.ofNullable(this.id);
	}
	
	public void setName(String name) {
		support.firePropertyChange(PROP_MODIFIED, this.name, name);
		this.name = name;
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
	
	public Set<EditablePlaylistFile> appendFiles(Set<File> files) {
		EditablePlaylistFolder rootFolder = createOrGetFolder(ROOT);
		long sortOrder = rootFolder.getFiles().size();
		return addFilesToFolder(files, rootFolder, sortOrder);
	}
	
	public Set<EditablePlaylistFile> appendFilesAfter(Set<File> files, EditablePlaylistFile file) {
		// sort all files in root directory
		EditablePlaylistFolder rootFolder = createOrGetFolder(ROOT);
		List<EditablePlaylistFile> rootFiles = new ArrayList<>(rootFolder.getFiles());
		rootFiles.sort(EditablePlaylistFileComparator.INSTANCE);
		
		// increase sort order of all files after the given file
		Long sortOrder = null; 
		for (EditablePlaylistFile f : rootFiles) {
			if (f.equals(file)) {
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
			throw new IllegalStateException("The given file <%s> is not in the list".formatted(file.getFileName()));
		}
		
		// increase by 1
		return addFilesToFolder(files, rootFolder, file.getSortOrder() + 1);
	}
	
	private Set<EditablePlaylistFile> addFilesToFolder(Set<File> files, EditablePlaylistFolder folder, long sortOrder) {
		Set<EditablePlaylistFile> newFiles = new HashSet<>();
		for (File file : files) {
			EditablePlaylistFile pf = new EditablePlaylistFile(file.getName(), folder, file.getAbsoluteFile(), sortOrder);
			newFiles.add(pf);
			support.firePropertyChange(PROP_ADD, null, file);
		}
		folder.getFiles().addAll(newFiles);
		return newFiles;
	}
	
	/**
	 * Copies files from another playlist to this one
	 */
	public Set<EditablePlaylistFile> copyFiles(Set<EditablePlaylistFile> files) {
		Set<EditablePlaylistFile> newFiles = new HashSet<>();
		for (EditablePlaylistFile file : files) {
			EditablePlaylistFolder newFolder = createOrGetFolder(file.getFolder().getPath());
			EditablePlaylistFile newFile = new EditablePlaylistFile(file.getFileName(), newFolder, file.getFile(), EditablePlaylistFile.DEFAULT_SORT_ORDER);
			newFolder.getFiles().add(newFile);
			support.firePropertyChange(PROP_ADD, null, file);
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
			support.firePropertyChange(PROP_MODIFIED, null, folder);
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
			EditablePlaylistFile newFile = new EditablePlaylistFile(file.getFileName(), moveToFolder, file.getFile(), file.getSortOrder());
			moveToFolder.getFiles().add(newFile);
			newFiles.add(newFile);
		}
		support.firePropertyChange(PROP_MODIFIED, null, moveToFolder);
		return newFiles;
	}
	
	public Optional<EditablePlaylistFile> renameFile(EditablePlaylistFile oldFile, String newFileName) {
		if (oldFile.getFileName().equals(newFileName)) {
			return Optional.empty();
		}
		
		EditablePlaylistFile.normalizeAndValidateName(newFileName);
		EditablePlaylistFolder folder = oldFile.getFolder();
		if (folder.getFiles().remove(oldFile)) {
			EditablePlaylistFile newFile = new EditablePlaylistFile(newFileName, folder, oldFile.getFile(), oldFile.getSortOrder());
			folder.getFiles().add(newFile);
			support.firePropertyChange(PROP_MODIFIED, oldFile, newFile);
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
			support.firePropertyChange(PROP_REMOVE, folder, null);
			return true;
		}
		return false;
	}
	
	public boolean remove(EditablePlaylistFile file) {
		EditablePlaylistFolder folder = file.getFolder();
		if (folder.getFiles().remove(file)) {
			support.firePropertyChange(PROP_REMOVE, file, null);
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
