package de.kobich.audiosolutions.core.service;

import java.util.Comparator;

import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;


/**
 * Compares FileDescriptors.
 * @author ckorn
 */
public class AudioFileDescriptorComparator extends DefaultFileDescriptorComparator implements Comparator<FileDescriptor> {
	/**
	 * Constructor
	 */
	public AudioFileDescriptorComparator() {}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(FileDescriptor o1, FileDescriptor o2) {
		AudioData audioData1 = o1.getMetaData(AudioData.class);
		AudioData audioData2 = o2.getMetaData(AudioData.class);
		if (audioData1 != null && audioData2 != null) {
			AudioState state1 = audioData1.getState();
			AudioState state2 = audioData2.getState();
			
			if (!AudioState.REMOVED.equals(state1) && !AudioState.REMOVED.equals(state2)) {
				// medium
				String medium1 = audioData1.getAttribute(AudioAttribute.MEDIUM);
				String medium2 = audioData2.getAttribute(AudioAttribute.MEDIUM);
				if (!medium1.equals(medium2)) {
					return medium1.compareTo(medium2);
				}
				// artist
				String artist1 = audioData1.getAttribute(AudioAttribute.ARTIST);
				String artist2 = audioData2.getAttribute(AudioAttribute.ARTIST);
				if (!artist1.equals(artist2)) {
					return artist1.compareTo(artist2);
				}
				// album
				String album1 = audioData1.getAttribute(AudioAttribute.ALBUM);
				String album2 = audioData2.getAttribute(AudioAttribute.ALBUM);
				if (!album1.equals(album2)) {
					return album1.compareTo(album2);
				}
				// disk
				if (audioData1.hasAttribute(AudioAttribute.DISK) && audioData2.hasAttribute(AudioAttribute.DISK)) {
					String disk1 = audioData1.getAttribute(AudioAttribute.DISK);
					String disk2 = audioData2.getAttribute(AudioAttribute.DISK);
					if (!disk1.equals(disk2)) {
						return disk1.compareTo(disk2);
					}
				}
				// track
				String track1 = audioData1.getAttribute(AudioAttribute.TRACK);
				String track2 = audioData2.getAttribute(AudioAttribute.TRACK);
				if (!track1.equals(track2)) {
					return track1.compareTo(track2);
				}
			}
		}
		
		return super.compare(o1, o2);
	}

}
