package de.kobich.audiosolutions.core.service.info;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.cddb.AudioCDDBService;
import de.kobich.audiosolutions.core.service.cddb.AudioCoverArt;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.net.IProxyProvider;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioInfoService {
	private static final Logger logger = Logger.getLogger(AudioInfoService.class);
	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "bmp", "gif", "png");

	@Autowired
	@Qualifier(value = IFileID3TagService.JAUDIO_TAGGER)
	private IFileID3TagService fileID3TagService;
	@Autowired
	private AudioCDDBService audioCDDBService;

	/**
	 * Returns the file info
	 * 
	 * @return
	 */
	public Optional<FileInfo> getFileInfo(List<FileDescriptor> files, boolean loadFromInternet, File coverArtRootDir, IProxyProvider proxyProvider,
			IServiceProgressMonitor monitor) throws AudioException {
		try {
//			Collections.sort(files, new DefaultFileDescriptorComparator());
			if (files.isEmpty()) {
				return Optional.empty();
			}
			
			// only consider first file
			FileDescriptor file = files.get(0);

			if (IMAGE_EXTENSIONS.contains(file.getExtension().toLowerCase())) {
				return Optional.of(new FileInfo(file, file.getFile(), null));
			}
			else {
					// prefer ID3 Tag artwork
					Optional<InputStream> is = fileID3TagService.readArtwork(Collections.singleton(file));
					if (is.isPresent()) {
						return Optional.of(new FileInfo(file, null, is.get()));
					}

					if (file.hasMetaData(AudioData.class)) {
						AudioData audioData = file.getMetaData(AudioData.class);
						String artist = audioData.getAttribute(AudioAttribute.ARTIST);
						String album = audioData.getAttribute(AudioAttribute.ALBUM);

						if (StringUtils.isNotBlank(artist) && !AudioData.DEFAULT_VALUE.equals(artist) && StringUtils.isNotBlank(album) && !AudioData.DEFAULT_VALUE.equals(album)) {
							File frontCover = getFrontCoverFile(artist, album, coverArtRootDir);
							if (frontCover.exists()) {
								return Optional.of(new FileInfo(file, frontCover, null));
							}

							// get artwork from Internet
							if (loadFromInternet) {
								AudioCoverArt coverArt = audioCDDBService.getCoverArt(artist, album, proxyProvider, monitor).orElse(null);
								if (coverArt != null && coverArt.getFront().isPresent()) {
									// stream is closed automatically
									FileUtils.copyInputStreamToFile(coverArt.getFront().get(), frontCover);
									return Optional.of(new FileInfo(file, frontCover, null));
								}
							}
						}
					}
				}
			return Optional.of(new FileInfo(file, null, null));
		}
		catch (Exception e) {
			logger.warn(e.getMessage(), e);
			throw new AudioException(AudioException.INTERNAL, e);
		}
	}

	private File getFrontCoverFile(String artist, String album, File coverArtRootDir) {
		return new File(coverArtRootDir, artist + "_" + album + "_front.jpg");
	}
	
}
