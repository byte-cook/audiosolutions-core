package de.kobich.audiosolutions.core.service.imexport.metadata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.IMetaData;
import de.kobich.component.file.imexport.MetaDataImportingFormatType;
import de.kobich.component.file.imexport.metadata.IMetaDataImporter;

/**
 * Audio data exporter.
 * @author ckorn
 */
@Service
public class AudioDataImporter implements IMetaDataImporter {
	private Map<AudioAttribute, String> attributes;
	
	public AudioDataImporter() {
		this.attributes = new HashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#getID()
	 */
	@Override
	public String getID() {
		return "audio";
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#supportFormat(de.simplesoft.audiosolutions.service.file.imexport.ImportingFormatType)
	 */
	@Override
	public boolean supportFormat(MetaDataImportingFormatType format) {
		return MetaDataImportingFormatType.XML.equals(format);
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#beginImport()
	 */
	@Override
	public void beginImport(MetaDataImportingFormatType format) {
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#beginImport(de.simplesoft.audiosolutions.domain.file.FileDescriptor)
	 */
	@Override
	public void beginFile(FileDescriptor fileDescriptor) {
		attributes.clear();
	}

	@Override
	public void importData(MetaDataImportingFormatType format, Map<String, String> map) throws IOException {
		if (MetaDataImportingFormatType.XML.equals(format)) {
			for (String key : map.keySet()) {
				String value = map.get(key);
				if (AudioXMLKey.STATE.key().equals(key)) {
//					audioData.setState(AudioState.getByName(value));
				}
				else {
					attributes.put(AudioAttribute.valueOf(key), value);
				}
			}
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#endImport()
	 */
	@Override
	public IMetaData endFile(FileDescriptor fileDescriptor) {
		IMetaData metaData = new AudioData(attributes);
		return metaData;
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImporter#endImport()
	 */
	@Override
	public void endImport() {
	}
}
