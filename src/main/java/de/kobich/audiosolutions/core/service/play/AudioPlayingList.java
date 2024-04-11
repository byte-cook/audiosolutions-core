package de.kobich.audiosolutions.core.service.play;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;

@Deprecated
public class AudioPlayingList implements IAudioPlayinglist {
	private final List<File> files;
	private int index;
	
	public AudioPlayingList() {
		this.files = new ArrayList<>();
		this.index = 0;
	}
	
	@Override
	public synchronized Optional<File> getStartFile() {
		return getCurrentFile();
	}
	
	public synchronized Optional<File> getCurrentFile() {
		if (0 <= this.index && this.index < this.files.size()) {
			return Optional.of(this.files.get(index));
		}
		return Optional.empty();
	}
	
	public void setFirstStartFile() {
		this.index = 0;
	}
	
	public synchronized void setStartFile(File file) {
		this.index = Math.max(0, this.files.indexOf(file));
	}
	
	public synchronized Optional<File> getNextFile(PlayListFlowType type) {
		switch (type) {
		case TRACK_FINISHED:
			return getNext(false);
		case NEXT_TRACK:
			return getNext(true);
		case PREVIOUS_TRACK:
			return getPrevious(true);
		case REPEAT_TRACK:
			return getCurrentFile();
		case STOP:
		default:
			return Optional.empty();
		}
	}
	
	private Optional<File> getNext(boolean loop) {
		if (loop) {
			index = (index + 1) % files.size();
		}
		else { 
			++ index;
		}
		return getCurrentFile();
	}
	
	private Optional<File> getPrevious(boolean loop) {
		if (loop) {
			if (index == 0) {
				index = files.size() - 1;
			}
			else {
				index = (index - 1) % files.size();
			}
		}
		else {
			-- index;
		}
		return getCurrentFile();
	}
	
//	public synchronized List<File> getFiles() {
//		return this.files;
//	}
	
	public synchronized void addFiles(List<File> files) {
		// do not allow duplicates
		this.removeFiles(files);
		this.files.addAll(files);
	}
	
	public synchronized void removeFiles(List<File> files) {
		this.files.removeAll(files);
	}

	@Override
	public Optional<File> getNextFile() {
		return getNextFile(PlayListFlowType.NEXT_TRACK);
	}

	@Override
	public Optional<File> getPreviousFile() {
		return getNextFile(PlayListFlowType.PREVIOUS_TRACK);
	}
}
