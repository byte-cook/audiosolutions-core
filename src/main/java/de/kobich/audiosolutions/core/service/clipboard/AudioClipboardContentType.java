package de.kobich.audiosolutions.core.service.clipboard;


public enum AudioClipboardContentType {
	TRACK("<track>"), 
	ALBUM("<album>"), 
	ALBUM_AND_PUBLICATION("<album> (<publication>)"), 
	ALBUM_AND_DISK("<album> (<disk>)"), 
	ARTIST("<artist>"), 
	MEDIUM("<medium>"),
	RELATIVE_PATH("<relative path>"),
	ABSOLUTE_PATH("<absolute path>"),
	;
	
	private final String structure;
	
	private AudioClipboardContentType(final String structure) {
		this.structure = structure;
	}

	/**
	 * @return the structure
	 */
	public String getStructure() {
		return structure;
	}
	
	/**
	 * Returns by name
	 * @param name
	 * @return
	 */
	public static AudioClipboardContentType getByName(String name) {
		for (AudioClipboardContentType type : AudioClipboardContentType.values()) {
			if (type.name().equals(name)) {
				return type;
			}
		}
		return null;
	}
}
