package de.kobich.audiosolutions.core.service.playlist;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public class EditablePlaylistFile {
	public static final long DEFAULT_SORT_ORDER = 0;
	
	@ToString.Include
	private final String fileName;
	@ToString.Include
	private final EditablePlaylistFolder folder;
	private final File file;
	
	@Nullable
	@Setter
	private Long sortOrder;
	
	protected EditablePlaylistFile(String fileName, EditablePlaylistFolder folder, File file, long sortOrder) {
		this.fileName = normalizeAndValidateName(fileName);
		this.folder = folder;
		this.file = file;
		this.sortOrder = sortOrder;
	}
	
	public static String normalizeAndValidateName(final String name) throws InvalidPathException {
		Paths.get(name);
		return name;
	}
}
