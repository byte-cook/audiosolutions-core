package de.kobich.audiosolutions.core.service.persist.domain;

import java.io.File;
import java.util.Date;

import de.kobich.audiosolutions.core.service.RatingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a track.
 */
@Entity
@Table(name = "track", uniqueConstraints = { 
	@UniqueConstraint(name = "UK_filePath", columnNames = { "file_path" })
	})
@NamedEntityGraph(
	name = Track.GRAPH,
	includeAllAttributes = true,
	attributeNodes = { 
		@NamedAttributeNode(value = "artist" /*, subgraph = "graph.track.artist" */),
		@NamedAttributeNode(value = "album", subgraph = "graph.track.album"),
		@NamedAttributeNode(value = "genre")
	}, 
	subgraphs = { 
		@NamedSubgraph(name = "graph.track.album", type=Album.class, 
			attributeNodes = { @NamedAttributeNode(value = "medium")}) 
	})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Track {
	public static final String GRAPH = "graph.track";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;
	
	@Column(name = "name", nullable = false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="artist_id", foreignKey=@ForeignKey(name = "FK_track_artist"), nullable = false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Artist artist;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "album_id", foreignKey = @ForeignKey(name = "FK_track_album"), nullable = false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Album album;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "genre_id", foreignKey = @ForeignKey(name = "FK_track_genre"), nullable = false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Genre genre;
	
	@Column(name = "disk_name", nullable = true)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String diskName;
	
	@Column(name = "no")
	@EqualsAndHashCode.Include
	@ToString.Include
	private int no;
	
	@Column(name = "format", nullable = true)
	private String format;
	
	@Column(name = "description", nullable = true)
	private String description;
	
	@Column(name = "file_path", nullable = false, unique = true)
	private String filePath;
	
	@Column(name = "file_path_on_medium", nullable = false)
	private String filePathOnMedium;
	
	@Column(name = "rating", nullable = true)
	@Enumerated(value = EnumType.STRING)
	private RatingType rating;
	
	@Column(name = "creation", nullable = false)
	@Temporal(TemporalType.DATE)
	@Setter(AccessLevel.PROTECTED)
	private Date creation;

	public Track() {
		this.creation = new Date();
	}
	
	public Track(String name) {
		this.setName(name);
		this.creation = new Date();
	}

	@Transient
	public File getFile() {
		return new File(this.getFilePath());
	}

}
