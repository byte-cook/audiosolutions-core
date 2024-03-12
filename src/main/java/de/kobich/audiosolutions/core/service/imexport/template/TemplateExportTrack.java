package de.kobich.audiosolutions.core.service.imexport.template;

import java.util.HashMap;
import java.util.Map;


public class TemplateExportTrack {
	private Map<TemplateExportKey, Object> attributes;
	
	public TemplateExportTrack() {
		this.attributes = new HashMap<TemplateExportKey, Object>();
	}
	
	public Object get(String keyName) {
		TemplateExportKey key = TemplateExportKey.toKey(keyName);
		if (key != null) {
			return get(key);
		}
		return null;
	}
	public Object get(TemplateExportKey key) {
		return attributes.get(key);
	}
	public Integer getAsInteger(TemplateExportKey key) {
		Object value = attributes.get(key);
		if (value instanceof Integer) {
			return (Integer) value;
		}
		return 0;
	}
	public String getAsString(TemplateExportKey key) {
		Object value = attributes.get(key);
		if (value != null) {
			return value.toString();
		}
		return "";
	}
	public boolean containsKey(TemplateExportKey key) {
		return attributes.containsKey(key);
	}
	public void put(TemplateExportKey key, Object value) {
		attributes.put(key, value);
	}

}
