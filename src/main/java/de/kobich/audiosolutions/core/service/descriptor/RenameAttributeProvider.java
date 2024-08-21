package de.kobich.audiosolutions.core.service.descriptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagResponse;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.descriptor.IRenameAttributeProvider;

public class RenameAttributeProvider implements IRenameAttributeProvider {
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final IFileID3TagService id3TagService;
	@Nullable
	private Map<MP3ID3TagType, String> id3Values;
	@Nullable
	private FileDescriptor id3Values4FileDescriptor;
	
	public RenameAttributeProvider(IFileID3TagService id3TagService) {
		this.id3TagService = id3TagService;
	}

	@Override
	public String getAttribute(FileDescriptor fileDescriptor, String attribute) {
		try {
			RenameFileDescriptorAttributeType md = RenameFileDescriptorAttributeType.getByName(attribute);
			switch (md) {
			case FILE_SIZE:
				return "" + fileDescriptor.getFile().length();
			case FILE_BASE_NAME: 
				return FilenameUtils.getBaseName(fileDescriptor.getFileName()); 
			case FILE_EXTENSION: 
				return FilenameUtils.getExtension(fileDescriptor.getFileName());
			case FILE_MODIFIED: 
				return DATE_FORMAT.format(new Date(fileDescriptor.getFile().lastModified()));
			case AUDIO_ALBUM:
			case AUDIO_ARTIST:
			case AUDIO_DISK:
			case AUDIO_ALBUM_PUBLICATION:
			case AUDIO_GENRE:
			case AUDIO_MEDIUM:
			case AUDIO_TRACK:
				if (md.getAttribute() != null && fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
					AudioAttribute audioAttribute = md.getAttribute();
					if (audioData.hasAttribute(audioAttribute)) {
						return audioData.getAttribute(audioAttribute, String.class);
					}
				}
				return "";
			case AUDIO_TRACK_NO:
				if (md.getAttribute() != null && fileDescriptor.hasMetaData(AudioData.class)) {
					AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
					AudioAttribute audioAttribute = md.getAttribute();
					if (audioData.hasAttribute(audioAttribute)) {
						Integer no = audioData.getAttribute(audioAttribute, Integer.class);
						// use at least 2 characters (add leading zero if required)
						return StringUtils.leftPad(String.valueOf(no), 2, '0');
					}
				}
				return "";
			case ID3_ALBUM:
			case ID3_ALBUM_YEAR:
			case ID3_ARTIST:
			case ID3_GENRE:
			case ID3_TRACK:
				if (md.getTagType() != null) {
					Map<MP3ID3TagType, String> id3Values = getID3TagValues(fileDescriptor);
					return id3Values.get(md.getTagType());
				}
				return "";
			case ID3_TRACK_NO:
				if (md.getTagType() != null) {
					Map<MP3ID3TagType, String> id3Values = getID3TagValues(fileDescriptor);
					if (!id3Values.isEmpty()) {
						String value = id3Values.get(md.getTagType());
						// use at least 2 characters (add leading zero if required)
						value = StringUtils.leftPad(value, 2, '0');
						return value;
					}
				}
				return "";
			}
			return null;
		}
		catch (AudioException exc) {
			return null;
		}		
	}
	
	@Override
	public synchronized void reload() {
		this.id3Values = null;
		this.id3Values4FileDescriptor = null;
	}

	private synchronized Map<MP3ID3TagType, String> getID3TagValues(FileDescriptor fileDescriptor) throws AudioException {
		if (id3Values == null || !this.id3Values4FileDescriptor.equals(fileDescriptor)) {
			ReadID3TagResponse response = id3TagService.readID3Tags(Set.of(fileDescriptor), null);
			this.id3Values = response.getSucceededFiles().getOrDefault(fileDescriptor, Map.of());
			this.id3Values4FileDescriptor = fileDescriptor;
		}
		return id3Values;
	}

}
