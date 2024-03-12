package de.kobich.audiosolutions.core.service.imexport;

import java.io.File;
import java.util.Set;

import de.kobich.commons.monitor.progress.ProgressMonitorRequest;
import de.kobich.component.file.FileDescriptor;

/**
 * Request to export files.
 * @author ckorn
 */
public class TemplateExportRequest extends ProgressMonitorRequest {
	private final Set<FileDescriptor> fileDescriptors;
	private final ExportTemplateType exportTemplateType;
	private File templateFile;
	private final File targetFile;

	/**
	 * Constructor
	 * @param targetFile the target file
	 * @param fileDescriptors
	 */
	public TemplateExportRequest(File targetFile, Set<FileDescriptor> fileDescriptors, ExportTemplateType exportTemplateType) {
		this.targetFile = targetFile;
		this.fileDescriptors = fileDescriptors;
		this.exportTemplateType = exportTemplateType;
	}

	/**
	 * @return the fileDescriptors
	 */
	public Set<FileDescriptor> getFileDescriptors() {
		return fileDescriptors;
	}

	/**
	 * @return the targetFile
	 */
	public File getTargetFile() {
		return targetFile;
	}

	/**
	 * @return the templateFile
	 */
	public File getTemplateFile() {
		return templateFile;
	}

	/**
	 * @return the exportTemplateType
	 */
	public ExportTemplateType getExportTemplateType() {
		return exportTemplateType;
	}

	/**
	 * @param templateFile the templateFile to set
	 */
	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}
}
