package de.kobich.audiosolutions.core.service.describe;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioDescriptionService {
	@Autowired
	private TrackRepository trackRepository;
	@Autowired
	private ArtistRepository artistRepository;
	@Autowired
	private AlbumRepository albumRepository;

	/**
	 * Returns the audio description
	 * @param request
	 * @return
	 */
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public Optional<String> getAudioDescription(GetAudioDescriptionRequest request) {
		List<FileDescriptor> fileDescriptors = request.getFileDescriptors();
		AudioDescriptionType type = request.getType();
		FileDescriptor fileDescriptor = fileDescriptors.get(0);
		
		if (!fileDescriptor.hasMetaData(AudioData.class)) {
			return Optional.empty();
		}
		AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
		if (!audioData.getState().isPersistent()) {
			return Optional.empty();
		}
		Long trackId = audioData.getTrackId();
		if (trackId == null) {
			return Optional.empty();
		}
		Track track = trackRepository.findById(trackId).orElse(null);
		if (track == null) {
			return Optional.empty();
		}

		if (AudioDescriptionType.ARTIST.equals(type)) {
			Artist artist = track.getArtist();
			if (artist != null) {
				return Optional.ofNullable(artist.getDescription());
			}
		}
		else if (AudioDescriptionType.ALBUM.equals(type)) {
			Album album = track.getAlbum();
			if (album != null) {
				return Optional.ofNullable(album.getDescription());
			}
		}
		else if (AudioDescriptionType.TRACK.equals(type)) {
			return Optional.ofNullable(track.getDescription());
		}
		return Optional.empty();
	
	}
	
	/**
	 * Sets the audio description
	 * @param request
	 */
	@Transactional(rollbackFor=AudioException.class)
	public boolean setAudioDescription(SetAudioDescriptionRequest request) {
		List<FileDescriptor> fileDescriptors = request.getFileDescriptors();
		AudioDescriptionType type = request.getType();
		String description = request.getDescription();
		if (StringUtils.isEmpty(description)) {
			description = null;
		}
		
		FileDescriptor fileDescriptor = fileDescriptors.get(0);
		if (!fileDescriptor.hasMetaData(AudioData.class)) {
			return false;
		}
		AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
		if (!audioData.getState().isPersistent()) {
			return false;
		}
		
		Long trackId = audioData.getTrackId();
		if (trackId == null) {
			return false;
		}
		
		Track track = trackRepository.findById(trackId).orElse(null);
		if (track == null) {
			return false;
		}

		if (AudioDescriptionType.ARTIST.equals(type)) {
			Artist artist = track.getArtist();
			if (artist != null) {
				artist.setDescription(description);
				artistRepository.save(artist);
				return true;
			}
		}
		else if (AudioDescriptionType.ALBUM.equals(type)) {
			Album album = track.getAlbum();
			if (album != null) {
				album.setDescription(description);
				albumRepository.save(album);
				return true;
			}
		}
		else if (AudioDescriptionType.TRACK.equals(type)) {
			track.setDescription(description);
			trackRepository.save(track);
			return true;
		}
		return false;
	
	}

}
