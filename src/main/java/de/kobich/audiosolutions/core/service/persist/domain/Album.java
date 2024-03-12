package de.kobich.audiosolutions.core.service.persist.domain;

import java.util.Date;
import java.util.Optional;

import de.kobich.audiosolutions.core.service.AudioDataChange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an album.
 */
@Entity
@Table(name="album")
@NamedEntityGraph(
		name = Album.GRAPH,
		includeAllAttributes = true,
		attributeNodes = { @NamedAttributeNode(value = "medium") } 
		)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Album {
	public static final String GRAPH = "graph.album";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name="name", nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="medium_id", foreignKey=@ForeignKey(name="FK_album_medium"), nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Medium medium;

	/**
	 * The artist of this album, can be null for collections
	 */
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="artist_id", foreignKey=@ForeignKey(name="FK_album_artist"), nullable=true)
	@EqualsAndHashCode.Include
	@ToString.Include
	@Getter(AccessLevel.NONE)
	private Artist artist;
	
	@Column(name="publication", nullable=true)
	@Temporal(TemporalType.DATE)
	private Date publication;
	
	@Column(name="description", nullable=true)
	private String description;
	
	/**
	 * Constructor
	 * @param name the name
	 */
	public Album(String name) {
		this.setName(name);
	}

	/**
	 * Returns an artist if available. Collection albums with several artists do not have an artist.  
	 * @see AudioDataChange#getAlbumIdentity()  
	 */
	public Optional<Artist> getArtist() {
		return Optional.ofNullable(this.artist);
	}

}
