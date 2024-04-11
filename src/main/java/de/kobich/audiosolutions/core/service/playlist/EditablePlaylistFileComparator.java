package de.kobich.audiosolutions.core.service.playlist;

import java.util.Comparator;

public class EditablePlaylistFileComparator implements Comparator<EditablePlaylistFile> {
	public static final EditablePlaylistFileComparator INSTANCE = new EditablePlaylistFileComparator();
	

	@Override
	public int compare(EditablePlaylistFile o1, EditablePlaylistFile o2) {
		int rc = Long.compare(o1.getSortOrder(), o2.getSortOrder());
		if (rc == 0) {
			rc = o1.getFile().compareTo(o2.getFile());
		}
		return rc;
	}

}
