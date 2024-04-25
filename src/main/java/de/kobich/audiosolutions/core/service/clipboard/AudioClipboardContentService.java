package de.kobich.audiosolutions.core.service.clipboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.component.file.FileDescriptor;

@Service
public class AudioClipboardContentService {
	private static final String NEW_LINE = System.getProperty("line.separator"); 

	/**
	 * Sets the clipboard content
	 * @param fileDescriptors
	 * @param type
	 */
	public String getClipboardContent(Set<FileDescriptor> fileDescriptors, AudioClipboardContentType type) {
		Set<String> content = new HashSet<String>();
		
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			AudioData audioData = null;
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				audioData = fileDescriptor.getMetaData(AudioData.class);
			}
			
			String text = null;
			switch (type) {
				case TRACK:
					text = audioData.getTrackIfNotDefault().orElse(null);
					break;
				case ALBUM:
					text = audioData.getAlbumIfNotDefault().orElse(null);
					break;
				case ALBUM_AND_PUBLICATION:
					text = audioData.getAlbumIfNotDefault().orElse(null);
					Date publication = audioData.getAlbumPublication().orElse(null);
					if (publication != null) {
						text += " (" + AudioAttributeUtils.convert2String(publication) + ")";
					}
					break;
				case ALBUM_AND_DISK:
					text = audioData.getAlbumIfNotDefault().orElse(null);
					String disk = audioData.getDisk().orElse(null);
					if (disk != null) {
						text += " (" + disk + ")";
					}
					break;
				case ARTIST:
					text = audioData.getArtistIfNotDefault().orElse(null);
					break;
				case MEDIUM:
					text = audioData.getMediumIfNotDefault().orElse(null);
					break;
				case RELATIVE_PATH:
					text = fileDescriptor.getRelativePath();
					break;
				case ABSOLUTE_PATH:
					text = fileDescriptor.getFile().getAbsolutePath();
					break;
			}
			
			if (text != null) {
				content.add(text);
			}
		}
		
		// sort
		List<String> contentList = new ArrayList<String>(content);
		Collections.sort(contentList);
		
		// convert to text
		return String.join(NEW_LINE, contentList);
	}
	
}
