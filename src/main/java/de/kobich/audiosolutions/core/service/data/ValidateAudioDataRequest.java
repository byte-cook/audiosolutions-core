package de.kobich.audiosolutions.core.service.data;

import java.util.List;
import java.util.Map;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.commons.misc.validate.rule.IValidationRule;
import de.kobich.commons.monitor.progress.ProgressMonitorRequest;
import de.kobich.component.file.FileDescriptor;

/**
 * File structure validation request.
 * @author ckorn
 */
public class ValidateAudioDataRequest extends ProgressMonitorRequest {
	private List<FileDescriptor> fileDescriptors;
	private Map<AudioAttribute, List<IValidationRule>> attribute2RulesMap;
	
	public ValidateAudioDataRequest(List<FileDescriptor> fileDescriptors, Map<AudioAttribute, List<IValidationRule>> attribute2RulesMap) {
		this.fileDescriptors = fileDescriptors;
		this.attribute2RulesMap = attribute2RulesMap;
	}

	/**
	 * @return the fileDescriptors
	 */
	public List<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}

	/**
	 * @return the attribute2RulesMap
	 */
	public Map<AudioAttribute, List<IValidationRule>> getAttribute2RulesMap() {
		return attribute2RulesMap;
	}
}
