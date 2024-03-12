package de.kobich.audiosolutions.core.service.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioAttribute2MP3ID3TagMapper;
import de.kobich.audiosolutions.core.service.AudioAttribute2StructureVariableMapper;
import de.kobich.audiosolutions.core.service.AudioAttributeUtils;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.AudioDataChange.AudioDataChangeBuilder;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioFileDescriptor;
import de.kobich.audiosolutions.core.service.AudioServiceUtils;
import de.kobich.audiosolutions.core.service.AudioState;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.ReadID3TagResponse;
import de.kobich.commons.misc.extract.ExtractStructureResponse;
import de.kobich.commons.misc.extract.Extractor;
import de.kobich.commons.misc.extract.IText;
import de.kobich.commons.misc.extract.StructureVariable;
import de.kobich.commons.monitor.progress.IServiceProgressMonitor;
import de.kobich.commons.monitor.progress.ProgressData;
import de.kobich.commons.monitor.progress.ProgressSupport;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.FileDescriptorResultSupport;
import de.kobich.component.file.FileDescriptorTextAdapter;
import de.kobich.component.file.TextComparator;
import de.kobich.component.file.descriptor.FileDescriptorResult;

/**
 * Service to manage audio data (such as artist, album).
 * @author ckorn
 */
@Service
public class AudioDataService {
	@Autowired
	@Qualifier(value=IFileID3TagService.JAUDIO_TAGGER)
	private IFileID3TagService fileID3TagService;

	/**
	 * Adds audio data to audio files (e.g. set Track/Album/Artist name)
	 * @param request the request
	 * @return audio data
	 */
	@Deprecated
	public void addAudioData(Set<AudioFileDescriptor> files, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Set Audio Data...", files.size()));

//		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
//		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());

		for (AudioFileDescriptor file : files) {
			FileDescriptor fileDescriptor = file.getFileDescriptor();
			Map<AudioAttribute, String> audioAttributes = file.getAudioDataValues();
			if (fileDescriptor == null) {
				continue;
			}
			AudioData audioData = null;

			// monitor message
			support.monitorSubTask(new ProgressData("Add audio data for: " + fileDescriptor.getFileName(), 1));

			if (fileDescriptor.hasMetaData(AudioData.class)) { 
				audioData = fileDescriptor.getMetaData(AudioData.class);
			}
			else {
				audioData = new AudioData();
				fileDescriptor.setMetaData(audioData);
			}

			boolean modified = false;
			// set audio attributes
			for (AudioAttribute attribute : AudioAttribute.values()) {
				boolean remove = false;
				String value = null;
				// values that are changed
				if (audioAttributes.containsKey(attribute)) {
					value = audioAttributes.get(attribute);
					if (value == null) {
						if (attribute.isRequired()) {
							value = AudioData.DEFAULT_VALUE;
						}
						else {
							remove = true;
						}
					}
				}
				// values that remain
				else if (audioData.hasAttribute(attribute)) {
					value = audioData.getAttribute(attribute);
				}
				
				// attribute is required
				if (attribute.isRequired() && StringUtils.isBlank(value)) {
					value = AudioData.DEFAULT_VALUE;
				}
				// if value is not null (e.g. no required attribute)
				if (remove) {
					if (!audioData.equalsAttribute(attribute, value)) {
						modified = true;
						audioData.removeAttribute(attribute);
					}
				}
				else if (value != null) {
					if (!audioData.equalsAttribute(attribute, value)) {
						modified = true;
						audioData.setAttribute(attribute, value);
					}
				}
			}
			if (modified) {
				updateAudioDataState(audioData);
			}
		}
		
		support.monitorEndTask(new ProgressData("Audio data successfully set"));
	}
	
	public Set<FileDescriptor> applyChanges(Set<FileDescriptor> fileDescriptors, AudioDataChange change, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Set Audio Data...", fileDescriptors.size()));

		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());

		Set<FileDescriptor> result = new HashSet<FileDescriptor>();
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			support.monitorSubTask(new ProgressData("Add audio data for: " + fileDescriptor.getFileName(), 1));
			
			AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
			if (audioData == null) {
				audioData = new AudioData();
				fileDescriptor.setMetaData(audioData);
			}
			
			audioData.applyChange(change);
			result.add(fileDescriptor);
		}

		support.monitorEndTask(new ProgressData("Audio data successfully set"));
		return result;
	}
	
	public Set<FileDescriptor> applyChanges(Set<AudioDataChange> changes, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Set Audio Data...", changes.size()));

//		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
//		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());

		Set<FileDescriptor> result = new HashSet<FileDescriptor>();
		for (AudioDataChange change : changes) {
			FileDescriptor fileDescriptor = change.getFileDescriptor();
			if (fileDescriptor == null) {
				support.monitorSubTask(new ProgressData("No file descriptor set", 1));
				continue;
			}
			support.monitorSubTask(new ProgressData("Add audio data for: " + fileDescriptor.getFileName(), 1));

			AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
			if (audioData == null) {
				audioData = new AudioData();
				fileDescriptor.setMetaData(audioData);
			}
			
			audioData.applyChange(change);
			result.add(fileDescriptor);
		}
		
		support.monitorEndTask(new ProgressData("Audio data successfully set"));
		return result;
	}

	/**
	 * Adds audio data to audio files (e.g. set Track/Album/Artist name) by file structure
	 * @param request the request
	 * @return audio data
	 */
	public void addAudioDataByStructure(Set<FileDescriptor> fileDescriptors, String fileStructurePattern, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Set Audio Data By Structure..."));
		
		Set<IText> texts = AudioServiceUtils.convert2Texts(fileDescriptors);
		Collection<StructureVariable> variables = AudioAttribute2StructureVariableMapper.getInstance().getVariables();
		ExtractStructureResponse structureResponse = Extractor.extract(texts, fileStructurePattern, variables, monitor);
		List<IText> failedTexts = structureResponse.getFailedTexts();
		Map<IText, Map<StructureVariable, String>> succeededFiles = structureResponse.getSucceededTexts();
		
		// monitor sub task
		support.monitorSubTask("Skipping failed files", failedTexts.size());

		AudioAttribute2StructureVariableMapper mapper = AudioAttribute2StructureVariableMapper.getInstance();
		Set<AudioDataChange> changes = new HashSet<>();
		List<IText> succeededTexts = new ArrayList<IText>(succeededFiles.keySet());
		Collections.sort(succeededTexts, new TextComparator());
		for (IText text : succeededTexts) {
			assert text instanceof FileDescriptorTextAdapter;
			FileDescriptor fileDescriptor = ((FileDescriptorTextAdapter) text).getFileDescriptor();
			AudioDataChangeBuilder builder = AudioDataChange.builder().fileDescriptor(fileDescriptor);
			
			// monitor sub task
			support.monitorSubTask("Set audio data to: " + fileDescriptor.getRelativePath(), 1);

			Map<StructureVariable, String> variableValues = succeededFiles.get(text);
			for (StructureVariable variable : variableValues.keySet()) {
				AudioAttribute attribute = mapper.getAudioAttribute(variable);
				if (attribute != null) {
					String value = variableValues.get(variable);
					AudioAttributeUtils.setValueInBuilder(builder, attribute, value);
				}
			}
			changes.add(builder.build());
		}
		applyChanges(changes, monitor);
		
		support.monitorEndTask("Audio data successfully set");
	}

	/**
	 * Adds audio data to mp3 audio files (e.g. set Track/Album/Artist name) by id3 tags
	 * @param request the request
	 * @return audio data
	 */
	public void addAudioDataByID3Tags(Set<FileDescriptor> fileDescriptors, @Nullable IServiceProgressMonitor monitor) throws AudioException {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Set Audio Data by ID3 tags..."));

		ReadID3TagResponse id3TagResponse = fileID3TagService.readID3Tags(fileDescriptors, null);
		List<FileDescriptor> failedFiles = id3TagResponse.getFailedFiles();
		Map<FileDescriptor, Map<MP3ID3TagType, String>> succeededFiles = id3TagResponse.getSucceededFiles();
		
		// monitor sub task
		ProgressData skippedData = new ProgressData("Skipping failed files", failedFiles.size());
		support.monitorSubTask(skippedData);

		AudioAttribute2MP3ID3TagMapper mapper = AudioAttribute2MP3ID3TagMapper.getInstance();
		Set<AudioDataChange> changes = new HashSet<>();
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(succeededFiles.keySet());
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			AudioDataChangeBuilder builder = AudioDataChange.builder().fileDescriptor(fileDescriptor);
			
			// monitor sub task
			ProgressData monitorData = new ProgressData("Set audio data to: " + fileDescriptor.getRelativePath(), 1);
			support.monitorSubTask(monitorData);

			Map<MP3ID3TagType, String> variableValues = succeededFiles.get(fileDescriptor);
			for (MP3ID3TagType variable : variableValues.keySet()) {
				AudioAttribute attribute = mapper.getAudioAttribute(variable);
				if (attribute != null) {
					String value = variableValues.get(variable);
					AudioAttributeUtils.setValueInBuilder(builder, attribute, value);
				}
			}
			changes.add(builder.build());
		}
		applyChanges(changes, monitor);

		support.monitorEndTask(new ProgressData("Audio data successfully set"));
	}

	/**
	 * Removes audio data from audio files (e.g. set Track/Album/Artist name)
	 * @param request the request
	 */
	public void removeAudioData(Collection<FileDescriptor> audioFiles, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Remove Audio Data..."));

		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>(audioFiles);
		Collections.sort(fileDescriptors, new DefaultFileDescriptorComparator());
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				support.monitorSubTask("Remove audio data of: " + fileDescriptor.getRelativePath(), 1);
				AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
				audioData.removeAll();
			}
		}

		support.monitorEndTask("Audio data successfully removed");
	}
	
	/**
	 * Modifies the import directory
	 * @param request
	 */
	public FileDescriptorResult modifyImportDirectories(Set<FileDescriptor> fileDescriptors, File importDirectory, IServiceProgressMonitor monitor) {
		ProgressSupport support = new ProgressSupport(monitor);
		support.monitorBeginTask(new ProgressData("Modify import directory..."));

		FileDescriptorResultSupport result = new FileDescriptorResultSupport();
		
		List<FileDescriptor> fileDescriptorList = new ArrayList<FileDescriptor>(fileDescriptors);
		Collections.sort(fileDescriptorList, new DefaultFileDescriptorComparator());
		
		for (FileDescriptor fileDescriptor : fileDescriptorList) {
			AudioData audioData = fileDescriptor.getMetaDataOptional(AudioData.class).orElse(null);
			if (audioData != null && audioData.getState().isPersistent()) {
				final File newFile = new File(importDirectory, fileDescriptor.getRelativePath());
				if (!newFile.exists()) {
					continue;
				}
				if (newFile.equals(fileDescriptor.getFile())) {
					continue;
				}

				support.monitorSubTask("Modify import directory of: " + fileDescriptor.getRelativePath(), 1);
				FileDescriptor clone = new FileDescriptor(newFile, fileDescriptor.getRelativePath());
				AudioData cloneAudioData = audioData.clone();
				cloneAudioData.setAsModified();
				clone.setMetaData(cloneAudioData);
				
				result.removedFileDescriptors.add(fileDescriptor);
				result.addedFileDescriptors.add(clone);
				result.replacedFiles.put(fileDescriptor, clone);
			}
		}
		result.setMissingAsFailed(fileDescriptorList);
		support.monitorEndTask("Import directory changed");
		
		return result.createFileDescriptorResult();
	}
	
	/**
	 * Updates the audio data state
	 * @param audioData
	 */
	private void updateAudioDataState(AudioData audioData) {
		for (AudioAttribute attribute : AudioAttribute.getRequiredAttributes()) {
			if (!audioData.hasAttribute(attribute) || audioData.getAttribute(attribute).equals(AudioData.DEFAULT_VALUE)) {
				AudioState state = audioData.getState().isTransient() ? AudioState.TRANSIENT_INCOMPLETE : AudioState.PERSISTENT_MODIFIED_INCOMPLETE;
				audioData.setState(state);
				return;
			}
		}
		AudioState state = audioData.getState().isTransient() ? AudioState.TRANSIENT : AudioState.PERSISTENT_MODIFIED;
		audioData.setState(state);
	}
	
}
