package de.kobich.audiosolutions.core.service.cddb;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.commons.monitor.progress.ProgressSupport;

public interface ICDDBRelease {
	
	String getId();
	
	String getAlbum();

	String getPublication();

	String getArtist();

	int getTrackCount();

	ICDDBTrack getTrack(int index);
	
	boolean isTrackLoaded();
	
	void loadTracks(ProgressSupport progressSupport) throws AudioException;

}
