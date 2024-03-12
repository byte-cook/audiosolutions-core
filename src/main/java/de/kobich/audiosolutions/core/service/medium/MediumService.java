package de.kobich.audiosolutions.core.service.medium;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioStatistics;
import de.kobich.audiosolutions.core.service.persist.domain.Medium;
import de.kobich.audiosolutions.core.service.persist.repository.AlbumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.ArtistRepository;
import de.kobich.audiosolutions.core.service.persist.repository.GenreRepository;
import de.kobich.audiosolutions.core.service.persist.repository.MediumRepository;
import de.kobich.audiosolutions.core.service.persist.repository.TrackRepository;

/**
 * Medium lend service.
 */
@Service
@Transactional(rollbackFor=AudioException.class)
public class MediumService {
	private static final Logger logger = Logger.getLogger(MediumService.class);
	@Autowired
	private MediumRepository mediumRepository;
	@Autowired
	private GenreRepository genreRepository;
	@Autowired
	private ArtistRepository artistRepository;
	@Autowired
	private AlbumRepository albumRepository;
	@Autowired
	private TrackRepository trackRepository;
	
	/**
	 * Lends mediums to <code>borrower</code>
	 * @param request
	 * @return response
	 */
	public MediumResponse lendMediums(Set<String> mediumNames, String borrower) {
		logger.debug("Lend mediums to " + borrower);
		List<String> succeededMediumNames = new ArrayList<String>();
		List<String> failedMediumNames = new ArrayList<String>();
		
		for (String mediumName : mediumNames) {
			Medium medium = mediumRepository.findFirstByName(mediumName).orElse(null);
			if (medium != null) {
				medium.setBorrower(borrower);
				medium.setBorrowingDate(new Date());
				medium.setLent(true);
				mediumRepository.save(medium);
				succeededMediumNames.add(mediumName);
			}
			else {
				failedMediumNames.add(mediumName);
			}
		}
		return new MediumResponse(succeededMediumNames, failedMediumNames);
	}

	/**
	 * Gives mediums back
	 * @param request
	 * @return response
	 */
	public MediumResponse returnMediums(Set<String> mediumNames) {
		logger.debug("Give mediums back");
		List<String> succeededMediumNames = new ArrayList<String>();
		List<String> failedMediumNames = new ArrayList<String>();
		
		for (String mediumName : mediumNames) {
			Medium medium = mediumRepository.findFirstByName(mediumName).orElse(null);
			if (medium != null && medium.isLent()) {
				medium.setBorrower(null);
				medium.setBorrowingDate(null);
				medium.setLent(false);
				mediumRepository.save(medium);
				succeededMediumNames.add(mediumName);
			}
			else {
				failedMediumNames.add(mediumName);
			}
		}
		return new MediumResponse(succeededMediumNames, failedMediumNames);
	}
	
	@Transactional(rollbackFor=AudioException.class, readOnly = true)
	public AudioStatistics getStatistics(Set<Medium> mediums) {
		return AudioStatistics.builder()
				.mediumCount(mediums.size())
				.genreCount(genreRepository.countByMediums(mediums))
				.artistCount(artistRepository.countByMediums(mediums))
				.albumCount(albumRepository.countByMediums(mediums))
				.trackCount(trackRepository.countByMediums(mediums)).build();
	}

}
