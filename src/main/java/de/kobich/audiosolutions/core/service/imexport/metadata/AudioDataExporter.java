package de.kobich.audiosolutions.core.service.imexport.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.kobich.audiosolutions.core.service.AudioAttribute;
import de.kobich.audiosolutions.core.service.AudioData;
import de.kobich.component.file.FileDescriptor;
import de.kobich.component.file.IMetaData;
import de.kobich.component.file.imexport.MetaDataExportingFormatType;
import de.kobich.component.file.imexport.metadata.IMetaDataExporter;

/**
 * Audio data exporter.
 * @author ckorn
 */
@Service
public class AudioDataExporter implements IMetaDataExporter {
	private IMetaData metaData;

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataImExporter#getID()
	 */
	@Override
	public String getID() {
		return "audio";
	}

	@Override
	public List<String> beginExport(MetaDataExportingFormatType format) throws IOException {
		List<String> keys = new ArrayList<String>();
		switch (format) {
			case XML:
				keys.add(AudioXMLKey.STATE.key());
				for (AudioAttribute attribute : AudioAttribute.values()) {
					keys.add(attribute.name());
				}
				break;
			case CSV:
				keys.add(AudioCSVTokenType.AUDIO_DATA.tag());
				keys.add(AudioCSVTokenType.STATE.tag());
				keys.add(AudioCSVTokenType.MEDIUM.tag());
				keys.add(AudioCSVTokenType.ARTIST.tag());
				keys.add(AudioCSVTokenType.ALBUM.tag());
				keys.add(AudioCSVTokenType.ALBUM_PUBLICATION.tag());
				keys.add(AudioCSVTokenType.DISK.tag());
				keys.add(AudioCSVTokenType.TRACK.tag());
				keys.add(AudioCSVTokenType.TRACK_FORMAT.tag());
				keys.add(AudioCSVTokenType.TRACK_NO.tag());
				keys.add(AudioCSVTokenType.GENRE.tag());
				keys.add(AudioCSVTokenType.RATING.tag());
				break;
		}
		return keys;
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataExporter#beginExport(de.simplesoft.audiosolutions.domain.file.FileDescriptor)
	 */
	@Override
	public void beginFile(FileDescriptor fileDescriptor) {
		metaData = fileDescriptor.getMetaData();
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataExporter#exportData(de.simplesoft.audiosolutions.service.file.imexport.ExportingFormatType,
	 * java.io.BufferedWriter)
	 */
	@Override
	public Map<String, String> exportData(MetaDataExportingFormatType format) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		if (metaData instanceof AudioData) {
			AudioData audioData = (AudioData) metaData;
	
			switch (format) {
				case XML:
					map.put(AudioXMLKey.STATE.key(), audioData.getState().name());
					for (AudioAttribute attribute : AudioAttribute.values()) {
						if (audioData.hasAttribute(attribute)) {
							String key = attribute.name();
							String value = audioData.getAttribute(attribute);
							map.put(key, value);
						}
					}
					break;
				case CSV:
					AudioAttribute2CSVTokenTypeMapper mapper = AudioAttribute2CSVTokenTypeMapper.getInstance();
					map.put(AudioCSVTokenType.AUDIO_DATA.tag(), getID());
					map.put(AudioCSVTokenType.STATE.tag(), audioData.getState().name());
					for (AudioAttribute attribute : AudioAttribute.values()) {
						if (audioData.hasAttribute(attribute)) {
							String key = mapper.getCSVTokenType(attribute).tag();
							String value = audioData.getAttribute(attribute);
							map.put(key, value);
						}
					}
					break;
			}
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataExporter#endExport()
	 */
	@Override
	public void endFile(FileDescriptor fileDescriptor) {}

	@Override
	public void endExport() {}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.exporting.IMetaDataExporter#supportMetaData(de.simplesoft.audiosolutions.domain.file.IMetaData)
	 */
	@Override
	public boolean supportMetaData(IMetaData metaData) {
		return metaData instanceof AudioData;
	}

	/*
	 * (non-Javadoc)
	 * @see de.simplesoft.audiosolutions.service.file.imexport.IMetaDataExporter#supportFormat(de.simplesoft.audiosolutions.service.file.imexport.ExportingFormatType)
	 */
	@Override
	public boolean supportFormat(MetaDataExportingFormatType format) {
		return MetaDataExportingFormatType.XML.equals(format) || MetaDataExportingFormatType.CSV.equals(format);
	}
}
