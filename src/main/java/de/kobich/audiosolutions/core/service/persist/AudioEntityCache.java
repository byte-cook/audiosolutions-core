package de.kobich.audiosolutions.core.service.persist;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.kobich.audiosolutions.core.service.AlbumIdentity;
import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.RatingType;
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
import de.kobich.commons.utils.CompareUtils;
import de.kobich.commons.utils.FilenameUtils;
import de.kobich.commons.utils.SQLUtils;
import de.kobich.component.file.FileDescriptor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Service
public class AudioEntityCache {
	private static final Logger logger = Logger.getLogger(AudioEntityCache.class);
	private final LoadingCache<String, Medium> mediumCache;
	private final LoadingCache<String, Artist> artistCache;
	private final LoadingCache<String, Genre> genreCache;
	private final LoadingCache<AlbumKey, Album> albumCache;
	private final TrackRepository trackRepository;
	
	public AudioEntityCache(MediumRepository mediumRepository, ArtistRepository artistRepository, GenreRepository genreRepository, AlbumRepository albumRepository, TrackRepository trackRepository) {
		this.mediumCache = CacheBuilder.newBuilder().build(new MediumCacheLoader(mediumRepository));
		this.artistCache = CacheBuilder.newBuilder().build(new ArtistCacheLoader(artistRepository));
		this.genreCache = CacheBuilder.newBuilder().build(new GenreCacheLoader(genreRepository));
		this.albumCache = CacheBuilder.newBuilder().build(new AlbumCacheLoader(albumRepository, trackRepository));
		this.trackRepository = trackRepository;
	}
	
	/**
	 * Checks all preconditions, e.g. there is no track in the DB with the same file path
	 */
	public void checkPreconditions(FileDescriptor fileDescriptor, AudioData audioData) throws AudioException {
		// track is transient
		if (audioData.getTrackId() == null) {
			String filePath = fileDescriptor.getFile().getAbsolutePath();
			if (trackRepository.findFirstByFilePath(filePath).isPresent()) {
				// avoid unique constraint exception
				throw new AudioException(AudioException.DUPLICATE_FILE_ERROR);
			}
		}
	}
	
	/**
	 * Returns a medium (depends only on the name, name is unique)
	 */
	public Medium getOrCreateMedium(AudioData audioData) throws ExecutionException {
		String name = audioData.getMedium().orElse(AudioData.DEFAULT_VALUE);
		return this.mediumCache.get(name);
	}

	@RequiredArgsConstructor
	private static class MediumCacheLoader extends CacheLoader<String, Medium> {
		private final MediumRepository mediumRepository;

		@Override
		public Medium load(String name) throws Exception {
			Medium medium = mediumRepository.findFirstByName(name).orElse(null);
			if (medium == null) {
				medium = new Medium(name);
				medium = mediumRepository.save(medium);
			}
			return medium;
		}
	}
	
	/**
	 * Returns an artist (depends only on the name, name is unique)
	 */
	public Artist getOrCreateArtist(AudioData audioData) throws ExecutionException {
		String name = audioData.getArtist().orElse(AudioData.DEFAULT_VALUE);
		return this.artistCache.get(name);
	}
	
	@RequiredArgsConstructor
	private static class ArtistCacheLoader extends CacheLoader<String, Artist> {
		private final ArtistRepository artistRepository;

		@Override
		public Artist load(String name) throws Exception {
			Artist artist = artistRepository.findFirstByName(name).orElse(null);
			if (artist == null) {
				artist = new Artist(name);
				artist = artistRepository.save(artist);
			}
			return artist;
		}
	}
	
	/**
	 * Returns a genre (depends only on the name, name is unique)
	 */
	public Genre getOrCreateGenre(AudioData audioData) throws ExecutionException {
		String name = audioData.getGenre().orElse(AudioData.DEFAULT_VALUE);
		return this.genreCache.get(name);
	}
	
	@RequiredArgsConstructor
	private static class GenreCacheLoader extends CacheLoader<String, Genre> {
		private final GenreRepository genreRepository;

		@Override
		public Genre load(String name) throws Exception {
			Genre genre = genreRepository.findFirstByName(name).orElse(null);
			if (genre == null) {
				genre = new Genre(name);
				genre = genreRepository.save(genre);
			}
			return genre;
		}
	}
	
	@RequiredArgsConstructor
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private static class AlbumKey {
		// Medium properties
		@EqualsAndHashCode.Include
		public final String name;
		@EqualsAndHashCode.Include
		public final Medium medium;
		@Nullable 
		@EqualsAndHashCode.Include
		public final Date albumPublication;
		
//		@Nullable 
//		@EqualsAndHashCode.Include
//		public final Artist artist;
		
		// either parentFolder or albumIdentifier are set
		@Nullable 
		@EqualsAndHashCode.Include
		public final File parentFolder;
		@Nullable 
		@EqualsAndHashCode.Include
		public final AlbumIdentity albumIdentifier;
		
		// not part of the cache key
		public final FileDescriptor fileDescriptor;
	}
	
	/**
	 * Returns an album (depends on the name AND the medium; name is NOT unique).
	 */
	public Album getOrCreateAlbum(FileDescriptor fileDescriptor, AudioData audioData, Medium medium) throws ExecutionException {
		String name = audioData.getAlbum().orElse(AudioData.DEFAULT_VALUE);
		AlbumKey albumKey;
		if (audioData.getAlbumIdentifier().isPresent()) {
			// artist is not set here, only required fields are used
			albumKey = new AlbumKey(name, medium, audioData.getAlbumPublication().orElse(null), null, audioData.getAlbumIdentifier().get(), fileDescriptor);
		}
		else {
			albumKey = new AlbumKey(name, medium, audioData.getAlbumPublication().orElse(null), fileDescriptor.getFile().getParentFile(), null, fileDescriptor);
		}
		return albumCache.get(albumKey);
	}
	
	@RequiredArgsConstructor
	private static class AlbumCacheLoader extends CacheLoader<AlbumKey, Album> {
		private final AlbumRepository albumRepository;
		private final TrackRepository trackRepository;

		@Override
		public Album load(AlbumKey key) throws Exception {
			Album album = findSuitableAlbum(key).orElse(null);
			if (album == null) {
				logger.info("Load album: create new one");
				album = new Album(key.name);
				album.setMedium(key.medium);
				// the album's artist is set in AudioPersistenceService.updateAlbumArtist()
				album.setPublication(key.albumPublication);
				album = albumRepository.save(album);
			}
			else {
				logger.info("Load album: use existing one with ID=" + album.getId());
				// set properties
				if (!CompareUtils.equals(key.albumPublication, album.getPublication())) {
					album.setPublication(key.albumPublication);
					album = albumRepository.save(album);
				}
			}
			return album;
		}
		
		/**
		 * Returns a suitable album or null. By default (albumIdentifier == null), all files in the same folder belong to the same album.
		 * For albums with tracks in different folders, you can use the {@link AlbumIdentity} object to show that they belong together.
		 * @param key
		 * @return
		 * @throws IOException
		 */
		private Optional<Album> findSuitableAlbum(AlbumKey key) throws IOException {
			if (key.albumIdentifier != null) {
				// return no candidate because the load method is only called once per key
				return Optional.empty();
			}

			/**
			 * Approach 1 (not used): Create a new album for each artist. 
			 * albumName + medium + artist are unique
			 * 
			 * This approach is no longer used because album collections are not supported at all.
			 */
//			List<Track> tracks = trackRepository.findByArtistAndAlbumAndMedium(key.artist, key.name, key.medium, page).getContent();
//			if (!tracks.isEmpty()) {
//				Track track = tracks.get(0);
//				// album artist must be available because albumIdentifier is not set
//				if (track.getAlbum().getArtist().isPresent()) {
//					return Optional.of(track.getAlbum());
//				}
//			}
			
			/**
			 * Approach 2: All files in the same folder belong to the same album.
			 * albumName + medium + parent path are unique
			 */
			AudioData audioData = key.fileDescriptor.getMetaData(AudioData.class);
			if (audioData != null) {
				// create parent path with wildcard: /media/Artist/Album/01-Track.mp3 -> /media/Artist/Album/% 
				String path = key.fileDescriptor.getFile().getAbsolutePath();
				// replace filename by wildcard
				path = path.replace(key.fileDescriptor.getFileName(), "");
				// escape SQL wildcards
				path = SQLUtils.escapeSQLWildcards(path);
				// add wildcard at the end
				String filePathWithWildcards = path + "%"; 
				// replace disk by wildcard
				String disk = audioData.getDisk().orElse(null);
				if (disk != null) {
					filePathWithWildcards = filePathWithWildcards.replace(disk, "%");
				}
				
				logger.info("Find album by path: " + filePathWithWildcards);
				final PageRequest page = PageRequest.ofSize(1);
				List<Track> trackByPaths = trackRepository.findByAlbumAndFilePath(key.name, key.medium, filePathWithWildcards, SQLUtils.LIKE_ESCAPE_CHAR, page).getContent();
				if (!trackByPaths.isEmpty()) {
					Track track = trackByPaths.get(0);
					
					// folder count could be different: "/album/%" matches both "/artist/file" and "/artist/album/file" 
					int folderCount = FilenameUtils.getParentFolders(key.fileDescriptor.getFile()).size();
					int trackFolderCount = FilenameUtils.getParentFolders(track.getFile()).size();
					if (folderCount != trackFolderCount) {
						return Optional.empty();
					}
					return Optional.of(track.getAlbum());
				}
			}
			return Optional.empty();
		}
	}

	/**
	 * Returns a track (depends on the name, the artist, the album, the medium, the disk; name is NOT unique)
	 */
	public Track getOrCreateTrack(FileDescriptor fileDescriptor, AudioData audioData, Artist artist, Album album, Genre genre) {
		String name = audioData.getTrack().orElseThrow();
		String filePath = fileDescriptor.getFile().getAbsolutePath();
		String filePathOnMedium = fileDescriptor.getRelativePath();
		
		// search track in DB
		Track track = null;
		if (audioData.getTrackId() != null) {
			track = trackRepository.findById(audioData.getTrackId()).orElse(null);
		}
		if (track == null) {
			// track is transient
			track = new Track();
		}
		
		// set dependent entities
		track.setName(name);
		track.setArtist(artist);
		track.setAlbum(album);
		track.setGenre(genre);
		// set properties
		track.setDiskName(audioData.getDisk().orElse(null));
		track.setFilePath(filePath);
		track.setFilePathOnMedium(filePathOnMedium);
		track.setFormat(audioData.getAttribute(AudioAttribute.TRACK_FORMAT));
		// track no
		String trackNoString = audioData.getAttribute(AudioAttribute.TRACK_NO);
		Integer trackNo = AudioAttributeUtils.convert2Integer(trackNoString);
		if (trackNo != null) {
			track.setNo(trackNo);
		}
		// rating
		String ratingTypeString = audioData.getAttribute(AudioAttribute.RATING);
		if (ratingTypeString != null) {
			RatingType ratingType = AudioAttributeUtils.convert2RatingType(ratingTypeString);
			track.setRating(ratingType);
		}
		return trackRepository.save(track);
	}
	
	public Collection<Album> getAllAlbums() {
		return this.albumCache.asMap().values();
	}
}
