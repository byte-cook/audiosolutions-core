package de.kobich.audiosolutions.core.service.play;

import java.io.File;
import java.util.Optional;

/**
 * The list of files to play.
 */
public interface IAudioPlayinglist {
	/**
	 * Returns the file to start playing with - e.g. the first file of the list or the last file that was played
	 * @return
	 */
	Optional<File> getStartFile();

	/**
	 * Returns the next file
	 * @return
	 */
	Optional<File> getNextFile();
	
	/**
	 * Returns the previous file
	 * @return
	 */
	Optional<File> getPreviousFile();
	
}