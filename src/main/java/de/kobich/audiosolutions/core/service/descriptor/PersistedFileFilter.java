package de.kobich.audiosolutions.core.service.descriptor;

import java.io.File;
import java.util.List;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import de.kobich.audiosolutions.core.service.persist.AudioPersistenceService;

public class PersistedFileFilter extends AbstractFileFilter {
	private final List<String> fileNames;

	public PersistedFileFilter(File startDirectory, AudioPersistenceService persistenceService) {
		fileNames = persistenceService.getFilenames(startDirectory);
	}

	@Override
	public boolean accept(File pathname) {
		return fileNames.contains(pathname.getAbsolutePath());
	}

}
