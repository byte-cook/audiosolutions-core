package de.kobich.audiosolutions.core.service.cddb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseGroupResultWs2;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.cddb.musicbrainz.MusicbrainzCDDBRelease;
import de.kobich.audiosolutions.core.service.cddb.musicbrainz.MusicbrainzFactory;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressCancelException;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.commons.net.IProxyProvider;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import fm.last.musicbrainz.coverart.CoverArt;
import fm.last.musicbrainz.coverart.CoverArtArchiveClient;
import fm.last.musicbrainz.coverart.CoverArtImage;
import fm.last.musicbrainz.coverart.impl.DefaultCoverArtArchiveClient;

/**
 * @see https://code.google.com/archive/p/musicbrainzws2-java/
 * @see https://code.google.com/archive/p/musicbrainzws2-java/wikis/Usage_Search.wiki
 */
@Service
public class AudioCDDBService {
	private static final Logger logger = Logger.getLogger(AudioCDDBService.class);
	public enum SearchDepth { HIGH, MEDIUM, SINGLE }
	
	/**
	 * Assigns cddb information to files
	 * @param fileDescriptors
	 * @param release
	 * @return
	 * @throws AudioException
	 */
	public List<AudioDataChange> assignCDDBTracks(Set<FileDescriptor> fileDescriptors, ICDDBRelease release, IProxyProvider proxyProvider, IServiceProgressMonitor monitor) throws AudioException {
		List<AudioDataChange> containers = new ArrayList<>(); 
		
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		
		int count = Math.max(release.getTrackCount(), fileDescriptorList.size());
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Assigning CDDB tracks");
		
		if (!release.isTrackLoaded()) {
			release.loadTracks(progressSupport);
		}
		
		for (int i = 0; i < count; ++i) {
			String fileName = "";
			FileDescriptor fileDescriptor = null;
			if (fileDescriptorList.size() > i) {
				fileDescriptor = fileDescriptorList.get(i);
				fileName = fileDescriptor.getFileName();
			}
			progressSupport.monitorSubTask("Retrieving track: " + fileName, 1);

			AudioDataChangeBuilder builder = AudioDataChange.builder().fileDescriptor(fileDescriptor);
			ICDDBTrack track = null;
			if (release.getTrackCount() > i) {
				track = release.getTrack(i);

				if (release.getAlbum() != null) {
					builder.album(release.getAlbum());
				}
				if (release.getPublication() != null) {
					Date publicationDate = AudioAttributeUtils.convert2Date(release.getPublication());
					if (publicationDate != null) {
						builder.albumPublication(publicationDate);
					}
				}
				
				if (track.getName() != null) {
					builder.track(track.getName());
				}
				if (track.getTrackNo() != 0) {
					builder.trackNo(track.getTrackNo());
				}
				if (track.getArtist() != null) {
					builder.artist(track.getArtist());
				}
				if (track.getDisk() != null) {
					builder.disk(track.getDisk());
				}
				if (track.getGenre() != null) {
					builder.genre(track.getGenre());
				}
			}
			containers.add(builder.build());
		}
		
		progressSupport.monitorEndTask("All tracks assigned");
		return containers;
	}
	
	public Optional<AudioCoverArt> getCoverArt(String artistName, String albumName, IProxyProvider proxyProvider, IServiceProgressMonitor monitor) throws AudioException {
		try {
			List<ICDDBRelease> releases = search(artistName, albumName, SearchDepth.SINGLE, proxyProvider, monitor);
			if (releases.isEmpty()) {
				return Optional.empty();
			}
	
			CoverArtArchiveClient client = new DefaultCoverArtArchiveClient();
			for (ICDDBRelease release : releases) {
				UUID mbid = UUID.fromString(release.getId());
		
				CoverArt coverArt = client.getByMbid(mbid);
				if (coverArt != null) {
					CoverArtImage front = coverArt.getFrontImage();
					CoverArtImage back = coverArt.getBackImage();
					InputStream frontIS = front != null ? front.getImage() : null;
					InputStream backIS = back != null ? back.getImage() : null;
					return Optional.of(new AudioCoverArt(frontIS, backIS));
				}
			}
			return Optional.empty();
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (Exception exc) {
			logger.error(exc.getMessage(), exc);
			throw new AudioException(AudioException.CDDB_ERROR, exc);
		}
	}

	/**
	 * Searches for CDDB releases
	 * @param artistName
	 * @param albumName
	 * @param proxyProvider
	 * @return
	 * @see http://forums.musicbrainz.org/viewtopic.php?id=4793
	 * @throws AudioException
	 */
	public List<ICDDBRelease> search(String artistName, String albumName, SearchDepth searchDepth, IProxyProvider proxyProvider, IServiceProgressMonitor monitor) throws AudioException {
		ProgressSupport progressSupport = new ProgressSupport(monitor);
		progressSupport.monitorBeginTask("Retrieving data from Musicbrainz", ProgressData.INDETERMINATE_MODE);

		checkSearchText(albumName);
		checkSearchText(artistName);

		MusicbrainzFactory factory = MusicbrainzFactory.createInstance(proxyProvider);
		List<ICDDBRelease> releases = searchReleases(factory, artistName, albumName, searchDepth, proxyProvider, progressSupport);
		progressSupport.monitorEndTask("All data successfully read");
		if (releases == null) {
			throw new AudioException(AudioException.ILLEGAL_STATE_ERROR);
		}
		return releases;
	}

	private List<ICDDBRelease> searchReleases(MusicbrainzFactory factory, String artistName, String albumName, SearchDepth searchDepth, IProxyProvider proxyProvider, ProgressSupport progressSupport) throws AudioException {
		try {
			boolean artist = !StringUtils.isEmpty(artistName);
			boolean album = !StringUtils.isEmpty(albumName);
			
			ReleaseGroup releaseGroupCtr = factory.createReleaseGroupController();
			if (artist && album) {
				releaseGroupCtr.search("\"" + albumName + "\" AND artist:" + artistName);
			}
			else if (artist && !album) {
				releaseGroupCtr.search("artist:" + artistName);
			}
			else if (!artist && album) {
				releaseGroupCtr.search("\"" + albumName + "\"");
			}
			else if (!artist && !album) {
				throw new AudioException(AudioException.CDDB_ILLEGAL_SEARCH_ERROR);
			}
			
			setSearchFilter(releaseGroupCtr, searchDepth);
//			releaseGroupWs = releaseGroupCtr.getFullSearchResultList(); 
			List<ReleaseGroupResultWs2> releaseGroupWs = releaseGroupCtr.getFirstSearchResultPage();
			
			if (releaseGroupWs.isEmpty()) {
				return new ArrayList<ICDDBRelease>();
			}

			List<ICDDBRelease> cddbReleases = new ArrayList<ICDDBRelease>();
			for (ReleaseGroupResultWs2 g : releaseGroupWs) {
				ReleaseGroupWs2 group = g.getReleaseGroup();
				
				progressSupport.monitorSubTask("Retrieving album: " + group.getTitle(), 1);

				cddbReleases.addAll(getReleases(group, factory, searchDepth, progressSupport));
			}
			return cddbReleases;
		}
		catch (ProgressCancelException exc) {
			throw exc;
		}
		catch (AudioException exc) {
			throw exc;
		}
		catch (Exception exc) {
			logger.error(exc.getMessage(), exc);
			throw new AudioException(AudioException.CDDB_ERROR, exc);
		}
	}
	
//	private List<ICDDBRelease> searchArtistReleases(MusicbrainzFactory factory, String artistName, String albumName, IProxyProvider proxyProvider, IServiceProgressMonitor monitor) throws AudioException {
//		Reject.ifTrue(StringUtils.isEmpty(artistName));
//		try {
//			// search artist
//			Artist artistCtr = factory.createArtistController();
//			artistCtr.search(artistName);
//			List<ArtistResultWs2> resultList = artistCtr.getFullSearchResultList();
//			if (resultList.isEmpty()) {
//				return new ArrayList<ICDDBRelease>();
//			}
//			ArtistWs2 artist = resultList.get(0).getArtist();
//			artistCtr = factory.createArtistController();
//			artistCtr.lookUp(artist);
//
//			// search album
//			List<ReleaseGroupWs2> releaseGroupWs = artistCtr.getFullReleaseGroupList();
//			if (releaseGroupWs.isEmpty()) {
//				return new ArrayList<ICDDBRelease>();
//			}
//
//			List<ICDDBRelease> cddbReleases = new ArrayList<ICDDBRelease>();
//			for (ReleaseGroupWs2 group : releaseGroupWs) {
//				if (StringUtils.isEmpty(albumName)) {
//					cddbReleases.addAll(getReleases(group, factory, monitor));
//				}
//				else {
//					boolean match = FilenameUtils.wildcardMatch(group.getTitle(), albumName, IOCase.INSENSITIVE);
//					if (match) {
//						cddbReleases.addAll(getReleases(group, factory, monitor));
//					}
//				}
//			}
//			return cddbReleases;
//		}
//		catch (ProgressCancelException exc) {
//			throw exc;
//		}
//		catch (Exception exc) {
//			logger.error(exc.getMessage(), exc);
//			throw new AudioException(AudioException.CDDB_ERROR, exc);
//		}
//	}

	private List<ICDDBRelease> getReleases(ReleaseGroupWs2 group, MusicbrainzFactory factory, SearchDepth searchDepth, ProgressSupport progressSupport) throws MBWS2Exception {
		List<ICDDBRelease> cddbReleases = new ArrayList<ICDDBRelease>();

		ReleaseGroup releaseGroupCtr = factory.createReleaseGroupController();
		setSearchFilter(releaseGroupCtr, searchDepth);

		releaseGroupCtr.lookUp(group);
		List<ReleaseWs2> releaseList = releaseGroupCtr.getFullReleaseList();
		for (ReleaseWs2 album : releaseList) {
			progressSupport.monitorSubTask("Retrieving album: " + group.getTitle(), 1);
			
//			// Note: to slow...
//			logger.info("load mediums");
//			Release releaseController = factory.createReleaseController();
//			MediumListWs2 mediumList = releaseController.getComplete(album).getMediumList();
//			for ( MediumWs2 m : mediumList.getMedia()) {
//				System.out.println(m.getTracksCount());
//			}
//			logger.info("load mediums endeee");
			
			MusicbrainzCDDBRelease cddbRelease = new MusicbrainzCDDBRelease(album, factory);
			cddbReleases.add(cddbRelease);
		}
		return cddbReleases;
	}
	
	private void checkSearchText(String text) throws AudioException {
		if (text == null) {
			return;
		}
		if (text.startsWith("*") || text.startsWith("?")) {
			throw new AudioException(AudioException.CDDB_ILLEGAL_SEARCH_ERROR);
		}
	}
	
	private void setSearchFilter(ReleaseGroup releaseGroupCtr, SearchDepth searchDepth) {
		switch (searchDepth) {
		case HIGH:
			releaseGroupCtr.getSearchFilter().setMinScore(70L);
			releaseGroupCtr.getSearchFilter().setLimit(20L);
			break;
		case MEDIUM:
			releaseGroupCtr.getSearchFilter().setMinScore(80L);
			releaseGroupCtr.getSearchFilter().setLimit(10L);
			break;
		case SINGLE:
		default:
			releaseGroupCtr.getSearchFilter().setMinScore(90L);
			releaseGroupCtr.getSearchFilter().setLimit(1L);
			break;
		}
	}

}
