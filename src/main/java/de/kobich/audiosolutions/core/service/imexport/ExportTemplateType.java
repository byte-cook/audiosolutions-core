package de.kobich.audiosolutions.core.service.imexport;

public enum ExportTemplateType {
	ARTIST_ALBUM("Artist / Album", "artist-album.vm"), 
	ARTIST_ALBUM_TRACK("Artist / Album / Track", "artist-album-track.vm"),
	TRACKS_HTML("Tracks HTML", "tracks-html.vm"),
	CUSTOMIZED("Customized", null);
	
	private final String name;
	private final String path;
	
	/**
	 * @param name
	 * @param path
	 */
	private ExportTemplateType(String name, String path) {
		this.name = name;
		this.path = path;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	
}