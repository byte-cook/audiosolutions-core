package de.kobich.audiosolutions.core.service.clipboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
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
					text = getAudioContent(audioData, AudioAttribute.TRACK);
					break;
				case ALBUM:
					text = getAudioContent(audioData, AudioAttribute.ALBUM);
					break;
				case ALBUM_AND_PUBLICATION:
					text = getAudioContent(audioData, AudioAttribute.ALBUM);
					String publication = getAudioContent(audioData, AudioAttribute.ALBUM_PUBLICATION);
					if (publication != null) {
						text += " (" + publication + ")";
					}
					break;
				case ALBUM_AND_DISK:
					text = getAudioContent(audioData, AudioAttribute.ALBUM);
					String disk = getAudioContent(audioData, AudioAttribute.DISK);
					if (disk != null) {
						text += " (" + disk + ")";
					}
					break;
				case ARTIST:
					text = getAudioContent(audioData, AudioAttribute.ARTIST);
					break;
				case MEDIUM:
					text = getAudioContent(audioData, AudioAttribute.MEDIUM);
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
		StringBuilder sb = new StringBuilder();
		for (String text : contentList) {
			if (sb.toString().isEmpty()) {
				sb.append(text);
			}
			else {
				sb.append(NEW_LINE);
				sb.append(text);
			}
		}
		return sb.toString();
	}
	
	private String getAudioContent(AudioData audioData, AudioAttribute attribute) {
		if (audioData != null && audioData.hasAttribute(attribute)) {
			String value = audioData.getAttribute(attribute);
			if (!AudioData.DEFAULT_VALUE.equals(value)) {
				return value;
			}
		}
		return null;
	}
}
