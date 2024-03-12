package de.kobich.audiosolutions.core.service.imexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.audiosolutions.core.service.AudioException;
import de.kobich.audiosolutions.core.service.AudioServiceUtils;
import de.kobich.audiosolutions.core.service.imexport.template.TemplateExportKey;
import de.kobich.audiosolutions.core.service.imexport.template.TemplateExportTrack;
import de.kobich.audiosolutions.core.service.imexport.template.TemplateExportTracks;
import de.kobich.audiosolutions.core.service.imexport.template.VelocityClassPathResouceLoader;
import de.kobich.commons.utils.StreamUtils;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

@Service
public class TemplateExportService {
	private static final Logger logger = Logger.getLogger(TemplateExportService.class);
	private static final String CHARSET_NAME = "UTF-8";

	/**
	 * Exports audio files by template
	 * @param request
	 * @throws AudioException
	 */
	public void exportFiles(TemplateExportRequest request) throws AudioException {
		List<FileDescriptor> fileDescriptors = new ArrayList<FileDescriptor>();
		fileDescriptors.addAll(request.getFileDescriptors());
		Collections.sort(fileDescriptors, new DefaultFileDescriptorComparator());

		List<TemplateExportTrack> trackList = new ArrayList<TemplateExportTrack>();
		for (FileDescriptor fileDescriptor : fileDescriptors) {
			if (fileDescriptor.hasMetaData(AudioData.class)) {
				AudioData audioData = fileDescriptor.getMetaData(AudioData.class);
				
				TemplateExportTrack track = new TemplateExportTrack();
				track.put(TemplateExportKey.TRACK, audioData.getAttribute(AudioAttribute.TRACK, String.class));
				track.put(TemplateExportKey.TRACK_NO, audioData.getAttribute(AudioAttribute.TRACK_NO, Integer.class));
				track.put(TemplateExportKey.ALBUM, audioData.getAttribute(AudioAttribute.ALBUM, String.class));
				track.put(TemplateExportKey.ARTIST, audioData.getAttribute(AudioAttribute.ARTIST, String.class));
				track.put(TemplateExportKey.MEDIUM, audioData.getAttribute(AudioAttribute.MEDIUM, String.class));
				track.put(TemplateExportKey.GENRE, audioData.getAttribute(AudioAttribute.GENRE, String.class));
				track.put(TemplateExportKey.DISK, audioData.getAttribute(AudioAttribute.DISK, String.class));
				
				trackList.add(track);
			}
		}
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			VelocityEngine engine = new VelocityEngine();
			// set logger
//			engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );
		    engine.setProperty("runtime.log.logsystem.log4j.logger", logger.getName());
			
		    // set resource loader
		    Template template = null;
		    ExportTemplateType type = request.getExportTemplateType();
		    switch (type) {
		    	case CUSTOMIZED:
		    		File templateFile = request.getTemplateFile();
		    		if (templateFile == null || !templateFile.exists()) {
		    			// template file missing
		    			throw new AudioException(AudioException.IO_ERROR);
		    		}
		    		engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, templateFile.getParent());
		    		engine.init();
		    		template = engine.getTemplate(templateFile.getName(), CHARSET_NAME);
		    		break;
		    	default:
		    		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "mine");
		    		engine.setProperty("mine.resource.loader.instance", new VelocityClassPathResouceLoader());
		    		engine.init();
		    		template = engine.getTemplate(type.getPath(), CHARSET_NAME);
		    		break;
		    }
	
			fw = new FileWriter(request.getTargetFile());
			bw = new BufferedWriter(fw);
	
			VelocityContext context = new VelocityContext();
			context.put("tracks", new TemplateExportTracks(trackList));
			for (TemplateExportKey k : TemplateExportKey.values()) {
				context.put(k.name(), k);
			}
			
			template.merge(context, bw);
		}
		catch (IOException e) {
			throw new AudioException(AudioException.IO_ERROR, e);
		}
		catch (VelocityException e) {
			throw new AudioException(AudioException.INTERNAL, e);
		}
		finally {
			StreamUtils.forceClose(bw);
			StreamUtils.forceClose(fw);
		}
	}

	/**
	 * Copies internal template to given file 
	 * @param type
	 * @param targetFile
	 * @return
	 * @throws AudioException 
	 */
	public File copyInternalExportTemplates(ExportTemplateType type, File targetFile) throws AudioException {
		if (ExportTemplateType.CUSTOMIZED.equals(type)) {
			return null;
		}
		return AudioServiceUtils.copyInternalFile(type.getPath(), TemplateExportService.class, targetFile);
	}
}
