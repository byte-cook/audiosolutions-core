package de.kobich.audiosolutions.core.service.persist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.persist.domain.Album;
import de.kobich.audiosolutions.core.service.persist.domain.Artist;
import de.kobich.audiosolutions.core.service.persist.domain.Genre;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.persist.domain.Track;
import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository;
import de.kobich.audiosolutions.core.service.persist.repository.GenreRepository;
import de.kobich.audiosolutions.core.service.persist.repository.MediumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.utils.CloneUtils;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

/**
 * Persistent audio data.
 * @author ckorn
 */
@Service
public class AudioPersistenceService {
	private static final Logger logger = Logger.getLogger(AudioPersistenceService.class);
	@Autowired
	private TrackRepository trackRepository;
	@Autowired
	private ArtistRepository artistRepository;
	@Autowired
	private AlbumRepository albumRepository;
	@Autowired
	private GenreRepository genreRepository;
	@Autowired
	private MediumRepository mediumRepository;
	@Autowired
	private AudioEntityCacheFactory entityCacheFactory;
	@Autowired
    private PlatformTransactionManager transactionManager;
	
	/**
	 * Persists the given file descriptors
	 * @param fileDescriptors
	 * @param monitor
	 * @return set of persisted files
	 * @throws AudioException
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Set<FileDescriptor> persist(Collection<FileDescriptor> fileDescriptors, IServiceProgressMonitor monitor) throws AudioException {
		final AudioEntityCache entityCache = entityCacheFactory.createCache();
		Map<FileDescriptor, AudioData> backupMap = new HashMap<>();
		
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask(new ProgressData("Saving audio data...", fileDescriptorList.size()));
		try {
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			
			Set<FileDescriptor> result = new HashSet<>();
			// split bulk inserts into partitions: https://hsqldb.org/doc/2.0/guide/deployment-chapt.html#dec_bulk_operations
			final List<List<FileDescriptor>> partitions = Lists.partition(fileDescriptorList, 1000);
			for (final List<FileDescriptor> partition : partitions) {
				Set<FileDescriptor> partitionResult = transactionTemplate.execute(status -> { 
					try {
						return persist(partition, entityCache, progressSupport, backupMap);
					} 
					catch (Exception e) {
						// convert to runtime exception
						throw new RuntimeException(e);
					}
				});
				result.addAll(partitionResult);
				// successfully committed: backup not needed anymore
				backupMap.clear();
			}
			
			transactionTemplate.executeWithoutResult(status -> {
				progressSupport.monitorSubTask("Update albums...", 0);
				updateAlbumArtist(entityCache);
			});
			transactionTemplate.executeWithoutResult(status -> {
				progressSupport.monitorSubTask("Removing orphaned data...", 0);
				deleteOrphanedData();
			});

			progressSupport.monitorEndTask("Audio data saved");
			return result;
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			// restore original state
			for (FileDescriptor fd : fileDescriptors) {
				AudioData ad = backupMap.get(fd);
				if (ad != null) {
					fd.setMetaData(ad);
				}
			}
			// return correct exception 
			if (ExceptionUtils.getRootCause(e) instanceof AudioException appExc) {
				throw appExc;
			}
			throw new AudioException(AudioException.INTERNAL);
		}
	}
	
	private Set<FileDescriptor> persist(List<FileDescriptor> fileDescriptorList, AudioEntityCache entityCache, ProgressSupport progressSupport, Map<FileDescriptor, AudioData> backupMap) throws AudioException, ExecutionException {
		Set<FileDescriptor> result = new HashSet<FileDescriptor>();
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			AudioData audioData = (AudioData) fileDescriptor.getMetaData(AudioData.class);
			if (audioData != null) {
				backupMap.put(fileDescriptor, CloneUtils.deepCopy(audioData));
				
				switch (audioData.getState()) {
					case PERSISTENT:
					case PERSISTENT_INCOMPLETE:
						// nothing to do
						progressSupport.monitorSubTask("Skipping file: " + fileDescriptor.getRelativePath(), 1);
						break;
					case PERSISTENT_MODIFIED:
					case PERSISTENT_MODIFIED_INCOMPLETE:
					case TRANSIENT:
					case TRANSIENT_INCOMPLETE:
						// insert or update
						progressSupport.monitorSubTask("Saving file: " + fileDescriptor.getRelativePath(), 1);
						
						entityCache.checkPreconditions(fileDescriptor, audioData);

						// 1. medium
						Medium medium = entityCache.getOrCreateMedium(audioData);
						// 2. artist
						Artist artist = entityCache.getOrCreateArtist(audioData);
						// 3. genre
						Genre genre = entityCache.getOrCreateGenre(audioData);
						// 4. album
						Album album = entityCache.getOrCreateAlbum(fileDescriptor, audioData, medium);
						// 5. track
						Track track = entityCache.getOrCreateTrack(fileDescriptor, audioData, artist, album, genre);
						audioData.setAsPersisted(track.getId());
						
						result.add(fileDescriptor);
						break;
					case REMOVED:
						progressSupport.monitorSubTask("Removing file: " + fileDescriptor.getRelativePath(), 1);
						
						Long trackId = audioData.getTrackId();
						if (trackId != null) {
							trackRepository.deleteById(trackId);
						}
						fileDescriptor.setMetaData(null);
						result.add(fileDescriptor);
						break;
				}
			}
			else {
				// nothing to do
				progressSupport.monitorSubTask("Skipping file: " + fileDescriptor.getRelativePath(), 1);
			}
		}
		return result;
	}
	
	/**
	 * Returns the count of items of the give attribute
	 * @param attribute
	 * @return
	 */
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public long getCount(AudioAttribute attribute) throws AudioException {
		switch (attribute) {
			case MEDIUM:
				return mediumRepository.count();
			case GENRE:
				return genreRepository.count();
			case ARTIST:
				return artistRepository.count();
			case ALBUM:
				return albumRepository.count();
			case TRACK:
				return trackRepository.count();
			default:
				return 0;
		}
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public AudioStatistics getStatistics() {
		return AudioStatistics.builder()
				.mediumCount(mediumRepository.count())
				.genreCount(genreRepository.count())
				.artistCount(artistRepository.count())
				.albumCount(albumRepository.count())
				.trackCount(trackRepository.count()).build();
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public List<String> getFilenames(File startDirectory) {
		String path = SQLUtils.escapeLikeParam(startDirectory.getAbsolutePath(), false, true);
		return trackRepository.findFilePathLike(path, SQLUtils.LIKE_ESCAPE_CHAR);
	}
	
	@Transactional(rollbackFor=AudioException.class)
	public void removeAll() throws AudioException {
		trackRepository.deleteAll();
		genreRepository.deleteAll();
		albumRepository.deleteAll();
		artistRepository.deleteAll();
		mediumRepository.deleteAll();
	}
	
	private void updateAlbumArtist(AudioEntityCache entityCache) {
		Collection<Album> albums = entityCache.getAllAlbums();
		for (Album album : albums) {
			List<Artist> albumArtists = artistRepository.findAllByAlbum(album);
			if (albumArtists.size() == 1) {
				Artist albumArtist = albumArtists.get(0);
				if (album.getArtist().isEmpty() || !album.getArtist().get().equals(albumArtist)) {
					logger.info(String.format("Set artist <%s> for album <%s>", albumArtist, album));
					album.setArtist(albumArtist);
					albumRepository.save(album);
				}
			}
			else if (albumArtists.size() > 1) {
				if (album.getArtist().isPresent()) {
					logger.info(String.format("Remove artist <%s> from album <%s>", album.getArtist().get(), album));
					album.setArtist(null);
					albumRepository.save(album);
				}
			}
		}
	}
	
	private void deleteOrphanedData() {
		logger.info("Deleting orphaned data...");
		// perform delete in reverse order 
		albumRepository.findAllWithoutTrack().forEach(a -> logger.info("Album without track: " + a));
		albumRepository.deleteAllWithoutTrack();

		genreRepository.findAllWithoutTrack().forEach(g -> logger.info("Genre without track: " + g));
		genreRepository.deleteAllWithoutTrack();
		
		artistRepository.findAllWithoutTrack().forEach(a -> logger.info("Artist without track: " + a));
		artistRepository.deleteAllWithoutTrack();
		
		mediumRepository.findAllWithoutTrack().forEach(m -> logger.info("Medium without track: " + m));
		mediumRepository.deleteAllWithoutTrack();
	}
	
}
