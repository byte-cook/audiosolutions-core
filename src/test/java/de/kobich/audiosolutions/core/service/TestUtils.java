package de.kobich.audiosolutions.core.service;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.component.file.FileDescriptor;

public class TestUtils {
	public static final String STONES = "Rolling Stones";
	public static final String BEGGARS_BANQUET = "Beggars Banquet";
	public static final String SYMPATHY_DEVIL = "sympathy for the devil";
	
	public static FileDescriptor createFileDescriptor(String fileName) {
		return new FileDescriptor(new File(fileName), fileName);
	}
	public static FileDescriptor createFileDescriptor(String fileName, String relativePath) {
		return new FileDescriptor(new File(fileName), relativePath);
	}
	public static Set<FileDescriptor> createFileDescriptors(String... fileNames) {
		Set<FileDescriptor> list = new HashSet<>();
		for (String name : fileNames) {
			list.add(createFileDescriptor(name));
		}
		return list;
	}
	
	public static FileDescriptor createFileDescriptor(String fileName, AudioDataBuilder audioDataBuilder) {
		return createFileDescriptor(new File(fileName), audioDataBuilder);
	}
	
	public static FileDescriptor createFileDescriptor(File file, @Nullable AudioDataBuilder audioDataBuilder) {
		FileDescriptor fd = new FileDescriptor(file, file.getAbsolutePath());
		if (audioDataBuilder != null) {
			fd.setMetaData(audioDataBuilder.build());
		}
		return fd;
	}
	
	public static AudioFileDescriptor createAudioFileDescriptor(String fileName, AudioDataBuilder audioDataBuilder) {
		FileDescriptor fd = new FileDescriptor(new File(fileName), fileName);
		return new AudioFileDescriptor(fd, audioDataBuilder.buildMap());
	}
	
	public static AudioDataChangeBuilder createAudioDataChangeBuilder(String fileName) {
		FileDescriptor fd = new FileDescriptor(new File(fileName), fileName);
		return AudioDataChange.builder().fileDescriptor(fd);
	}
	
	public static void printFileDescriptors(Collection<FileDescriptor> files) {
		for (FileDescriptor file : files) {
			AudioData audioData = file.getMetaData(AudioData.class);
			String artist = audioData.getArtist().orElse(AudioData.DEFAULT_VALUE);
			String medium = audioData.getMedium().orElse(AudioData.DEFAULT_VALUE);
			String album = audioData.getAlbum().orElse(AudioData.DEFAULT_VALUE);
			String genre = audioData.getGenre().orElse(AudioData.DEFAULT_VALUE);
			String track = audioData.getTrack().orElse(AudioData.DEFAULT_VALUE);
			System.out.println(String.format("%s : Artist=%s | Medium=%s | Album=%s | Genre=%s | Track=%s", file.getFileName(), artist, medium, album, genre, track));
		}
	}
}
