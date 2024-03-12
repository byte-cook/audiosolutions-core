package de.kobich.audiosolutions.core.service.imexport.metadata;

public enum AudioXMLKey {
	STATE("STATE", false);
	
	private String name;
	
	private AudioXMLKey(String name, boolean isTag) {
		this.name = name;
	}
	
	public String key() {
		return name;
	}
}
