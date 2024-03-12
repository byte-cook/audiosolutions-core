package de.kobich.audiosolutions.core.service.imexport.template;

public enum TemplateExportKey {
	GENRE("genre"), MEDIUM("medium"), ARTIST("artist"), ALBUM("album"), DISK("disk"), TRACK("track"), TRACK_NO("trackno");
	
	private final String id;
	
	private TemplateExportKey(String id) {
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public static TemplateExportKey toKey(Object o) {
		if (o instanceof TemplateExportKey) {
			return (TemplateExportKey) o;
		}
		return null;
	}
	public static TemplateExportKey toKey(String key) {
		for (TemplateExportKey k : TemplateExportKey.values()) {
			if (k.getId().equals(key)) {
				return k;
			}
		}
		return null;
	}
}