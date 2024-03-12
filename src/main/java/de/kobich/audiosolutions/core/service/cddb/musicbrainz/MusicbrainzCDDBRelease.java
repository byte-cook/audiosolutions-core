package de.kobich.audiosolutions.core.service.cddb.musicbrainz;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.musicbrainz.controller.Release;
import org.musicbrainz.model.MediumListWs2;
import org.musicbrainz.model.TrackWs2;
import org.musicbrainz.model.entity.ReleaseWs2;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.cddb.ICDDBRelease;
import de.kobich.audiosolutions.core.service.cddb.ICDDBTrack;
import de.kobich.commons.monitor.progress.ProgressSupport;

public class MusicbrainzCDDBRelease implements ICDDBRelease {
	private static final Logger logger = Logger.getLogger(MusicbrainzCDDBRelease.class);
	private final ReleaseWs2 album;
	private final MusicbrainzFactory factory;
	private final List<ICDDBTrack> tracks;

	public MusicbrainzCDDBRelease(ReleaseWs2 album, MusicbrainzFactory factory) {
		this.album = album;
		this.factory = factory;
		this.tracks = new ArrayList<ICDDBTrack>();
	}

	@Override
	public String getAlbum() {
		return album.getTitle();
	}

	@Override
	public String getPublication() {
		return album.getYear();
	}

	@Override
	public String getArtist() {
		return album.getArtistCreditString();
	}
	
	@Override
	public String getId() {
		return album.getId();
	}

	@Override
	public int getTrackCount() {
		return album.getTracksCount();
	}

	@Override
	public ICDDBTrack getTrack(int index) {
		if (tracks.size() > index) {
			return tracks.get(index);
		}
		return null;
	}

	@Override
	public void loadTracks(ProgressSupport progressSupport) throws AudioException {
		try {
			progressSupport.monitorSubTask("Retrieving album...", 1);
			Release releaseController = factory.createReleaseController();
			
			progressSupport.monitorSubTask("Retrieving mediums...", 1);
			MediumListWs2 mediumList = releaseController.getComplete(album).getMediumList();
			
			progressSupport.monitorSubTask("Retrieving tracks...", 1);
			List<TrackWs2> trackList = mediumList.getCompleteTrackList();
			for (TrackWs2 track : trackList) {
				MusicbrainzCDDBTrack cddbTrack = new MusicbrainzCDDBTrack(track, mediumList);
				tracks.add(cddbTrack);
				
				progressSupport.monitorSubTask("Retrieving track: " + cddbTrack.getName(), 1);
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new AudioException(AudioException.CDDB_ERROR);
		}
	}

	@Override
	public boolean isTrackLoaded() {
		return !tracks.isEmpty();
	}

}
