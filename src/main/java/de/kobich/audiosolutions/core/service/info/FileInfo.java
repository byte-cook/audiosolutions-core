package de.kobich.audiosolutions.core.service.info;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.lang.Nullable;

import de.kobich.component.file.FileDescriptor;

public class FileInfo {
	private final FileDescriptor fileDescriptor;
	@Nullable
	private final File artworkFile;
	@Nullable
	private final InputStream artwork;
	
	public FileInfo(FileDescriptor fileDescriptor, File artworkFile, InputStream artwork) {
		this.fileDescriptor = fileDescriptor;
		this.artworkFile = artworkFile;
		this.artwork = artwork;
	}

	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	public Optional<File> getArtworkFile() {
		return Optional.ofNullable(artworkFile);
	}

	public Optional<InputStream> getArtwork() {
		return Optional.ofNullable(artwork);
	}
}
