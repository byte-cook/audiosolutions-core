package de.kobich.audiosolutions.core.service.playlist;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import de.kobich.commons.utils.RelativePathUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class EditablePlaylistFolder {
	@EqualsAndHashCode.Include
	@ToString.Include
	private final String path;
	private final Set<EditablePlaylistFile> files;
	
	protected EditablePlaylistFolder(String path) {
		this.path = EditablePlaylistFolder.normalizeAndValidatePath(path);
		this.files = new HashSet<>();
	}

	public static String normalizeAndValidatePath(final String path) throws InvalidPathException {
		// normalize path
		String tmpRelPath = path;
		tmpRelPath = RelativePathUtils.convertBackslashToSlash(tmpRelPath);
		tmpRelPath = RelativePathUtils.ensureStartingSlash(tmpRelPath);
		// validate path
		Paths.get(tmpRelPath);
		return tmpRelPath;
	}

}
