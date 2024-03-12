package de.kobich.audiosolutions.core.service.cddb.musicbrainz;

import java.util.List;

import org.musicbrainz.model.MediumListWs2;
import org.musicbrainz.model.TagWs2;
import org.musicbrainz.model.TrackWs2;

import de.kobich.audiosolutions.core.service.cddb.ICDDBTrack;

public class MusicbrainzCDDBTrack implements ICDDBTrack {
	private final TrackWs2 track;
	private final MediumListWs2 mediumList;

	public MusicbrainzCDDBTrack(TrackWs2 track, MediumListWs2 mediumList) {
		this.track = track;
		this.mediumList = mediumList;
	}

	@Override
	public String getName() {
		return track.getRecording().getTitle();
	}

	@Override
	public int getTrackNo() {
		return track.getPosition();
	}

	@Override
	public String getArtist() {
		return track.getRecording().getArtistCreditString();
	}

	@Override
	public String getGenre() {
		List<TagWs2> tags = track.getRecording().getTags();
		if (tags.isEmpty()) {
			return null;
		}
		return tags.get(0).getName();
	}

	@Override
	public String getDisk() {
		for (TrackWs2 track : mediumList.getCompleteTrackList()) {
			String trackName = track.getRecording().getTitle();
			if (trackName != null && trackName.equals(getName())) {
				return track.getMediumStr();
			}
		}
//		for (DiscWs2 disc : mediumList.getCompleteDiscList()) {
//			System.out.println(disc.getDiscId());
//			System.out.println(disc.getMediumStr());
//			System.out.println(disc.getTracks().size());
//			for (DiscTrackWs2 track : disc.getTracks()) {
//				System.out.println(track.getTracknum());
//			}
//		}
		return null;
	}

}
