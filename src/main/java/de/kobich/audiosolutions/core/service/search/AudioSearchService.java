package de.kobich.audiosolutions.core.service.search;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository.AlbumName;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository.ArtistName;
import de.kobich.audiosolutions.core.service.persist.repository.GenreRepository;
import de.kobich.audiosolutions.core.service.persist.repository.GenreRepository.GenreName;
import de.kobich.audiosolutions.core.service.persist.repository.MediumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.MediumRepository.MediumName;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository.TrackName;
import de.kobich.audiosolutions.core.service.persist.repository.TrackSearchRepository;
import de.kobich.commons.Reject;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.component.file.FileDescriptor;

/**
 * Search service.
 */
@Service
@Transactional(rollbackFor=AudioException.class, readOnly = true)
public class AudioSearchService {
	private static final Logger logger = Logger.getLogger(AudioSearchService.class);
	@Autowired
	private TrackRepository trackRepository;
	@Autowired
	private TrackSearchRepository trackSearchRepository;
	@Autowired
	private ArtistRepository artistRepository;
	@Autowired
	private AlbumRepository albumRepository;
	@Autowired
	private GenreRepository genreRepository;
	@Autowired
	private MediumRepository mediumRepository;
	
	/**
	 * Searches for tracks
	 * @param request
	 * @return
	 */
	public Set<FileDescriptor> search(AudioSearchQuery query, IServiceProgressMonitor monitor) {
		// monitor start
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Searching tracks...");
		
		List<Track> tracks = trackSearchRepository.findByQuery(query);
		
		Set<FileDescriptor> fileDescriptors = convertTracks(tracks, progressSupport);
		
		// monitor end
		progressSupport.monitorEndTask("Searching finished");
		
		return fileDescriptors;
	}
	
	/**
	 * Searches for tracks
	 * @param artistNames
	 * @return
	 */
	public Set<FileDescriptor> searchByArtists(Set<String> artistNames, IServiceProgressMonitor monitor) {
		Reject.ifEmpty(artistNames);
		
		// monitor start
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Searching tracks...");
		
		List<Track> tracks = trackRepository.findByArtist(artistNames);
		Set<FileDescriptor> fileDescriptors = convertTracks(tracks, progressSupport);
		
		// monitor end
		progressSupport.monitorEndTask("Searching finished");
		
		return fileDescriptors;
	}

	/**
	 * Searches for tracks
	 * @param mediumNames
	 * @param monitor
	 * @return
	 */
	public Set<FileDescriptor> searchByMediums(Set<String> mediumNames, IServiceProgressMonitor monitor) {
		Reject.ifEmpty(mediumNames);
		
		// monitor start
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Searching tracks...");
		
		List<Track> tracks = trackRepository.findByMedium(mediumNames);
		Set<FileDescriptor> fileDescriptors = convertTracks(tracks, progressSupport);
		
		// monitor end
		progressSupport.monitorEndTask("Searching finished");
		
		return fileDescriptors;
	}
	
	private Set<FileDescriptor> convertTracks(List<Track> tracks, ProgressSupport progressSupport) {
		Set<FileDescriptor> fileDescriptors = new HashSet<FileDescriptor>();
		for (Track track : tracks) {
			logger.debug("Create file descriptor: " + track.getName());
			File file = track.getFile();
			FileDescriptor fileDescriptor = new FileDescriptor(file, track.getFilePathOnMedium());
			AudioData audioData = new AudioData(track);
			fileDescriptor.setMetaData(audioData);
			
			fileDescriptors.add(fileDescriptor);
			
			ProgressData subTask = new ProgressData("Found track #" + fileDescriptors.size() + ": " + fileDescriptor.getFileName() + " \n" +
					track.getArtist().getName() + " - " + track.getAlbum().getName());
			progressSupport.monitorSubTask(subTask);
		}
		return fileDescriptors;
	}
	
	/**
	 * Searches for artists
	 * @param artistName if artistName is null, all artists are returned
	 * @return
	 */
	public List<Artist> searchArtists(@Nullable String artistName) {
		List<Artist> artists = null;
		if (StringUtils.isNotBlank(artistName)) {
			artists = artistRepository.findAllByNameLikeIgnoreCase(SQLUtils.escapeLikeParam(artistName, true, true));
		}
		else {
			artists = artistRepository.findAll();
		}
		return artists;
	}

	/**
	 * Searches for mediums
	 * @param mediumName
	 * @return
	 */
	public List<Medium> searchMediums(@Nullable String mediumName) {
		List<Medium> mediums = null;
		if (StringUtils.isNotBlank(mediumName)) {
			mediums = mediumRepository.findAllByNameLikeIgnoreCase(SQLUtils.escapeLikeParam(mediumName, true, true));
		}
		else {
			mediums = mediumRepository.findAll();
		}
		return mediums;
	}

	/**
	 * Searches for similar entity names depending on the audio attribute
	 * @param attribute the audio attribute
	 * @param name the name to search for similar entities
	 * @param maxNumber
	 * @return
	 */
	public List<String> searchProposals(AudioAttribute attribute, String name, /*int position, */int maxNumber) {
		List<String> proposals = findProposals(attribute, name);
		return proposals.stream().filter(s -> !AudioData.DEFAULT_VALUE.equals(s)).limit(maxNumber).toList();
	}
	
	private List<String> findProposals(AudioAttribute attribute, String name) {
		String queryName = SQLUtils.escapeLikeParam(name, true, true);
		switch (attribute) {
			case ALBUM:
				return albumRepository.findAllByNameLikeIgnoreCaseOrderByName(queryName, AlbumName.class).stream().map(AlbumName::getName).toList();
			case ARTIST:
				return artistRepository.findAllByNameLikeIgnoreCaseOrderByName(queryName, ArtistName.class).stream().map(ArtistName::getName).toList();
			case GENRE:
				return genreRepository.findAllByNameLikeIgnoreCaseOrderByName(queryName, GenreName.class).stream().map(GenreName::getName).toList();
			case MEDIUM: 
				return mediumRepository.findAllByNameLikeIgnoreCaseOrderByName(queryName, MediumName.class).stream().map(MediumName::getName).toList();
			case TRACK:
				return trackRepository.findAllByNameLikeIgnoreCaseOrderByName(queryName, TrackName.class).stream().map(TrackName::getName).toList();
			default: 
				break;
		}
		return List.of();
	}

}
