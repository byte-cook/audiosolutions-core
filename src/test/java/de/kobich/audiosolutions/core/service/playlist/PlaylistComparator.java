package de.kobich.audiosolutions.core.service.playlist;

import java.util.Comparator;

import de.kobich.audiosolutions.core.service.playlist.repository.PlaylistFile;

public class PlaylistComparator implements Comparator<PlaylistFile> {

	@Override
	public int compare(PlaylistFile o1, PlaylistFile o2) {
		int rc = Long.compare(o1.getSortOrder(), o2.getSortOrder());
		if (rc != 0) {
			return rc;
		}
		return o1.getName().compareTo(o2.getName());
	}

}
