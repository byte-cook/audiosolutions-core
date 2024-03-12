package de.kobich.audiosolutions.core.service.play;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.kobich.audiosolutions.core.service.play.player.AudioPlayerResponse.PlayListFlowType;
import de.kobich.component.file.FileDescriptor;

public class AudioPlayList {
	private final List<FileDescriptor> files;
	private int index;
	
	public AudioPlayList() {
		this.files = new ArrayList<FileDescriptor>();
		this.index = 0;
	}
	
	public synchronized Optional<FileDescriptor> getCurrentFile() {
		if (0 <= this.index && this.index < this.files.size()) {
			return Optional.of(this.files.get(index));
		}
		return Optional.empty();
	}
	
	public void setFirstStartFile() {
		this.index = 0;
	}
	
	public synchronized void setStartFile(FileDescriptor file) {
		this.index = Math.max(0, this.files.indexOf(file));
	}
	
	public synchronized Optional<FileDescriptor> getFile(PlayListFlowType type) {
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
	
	private Optional<FileDescriptor> getNext(boolean loop) {
		if (loop) {
			index = (index + 1) % files.size();
		}
		else { 
			++ index;
		}
		return getCurrentFile();
	}
	
	private Optional<FileDescriptor> getPrevious(boolean loop) {
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
	
	public synchronized List<FileDescriptor> getFiles() {
		return this.files;
	}
	
	public synchronized void addFile(FileDescriptor file) {
		this.addFiles(Collections.singletonList(file));
	}
	
	public synchronized void addFiles(List<FileDescriptor> files) {
		// do not allow duplicates
		this.removeFiles(files);
		this.files.addAll(files);
	}
	
	public synchronized void removeFile(FileDescriptor file) {
		this.removeFiles(Collections.singletonList(file));
	}
	
	public synchronized void removeFiles(List<FileDescriptor> files) {
		this.files.removeAll(files);
	}
}
