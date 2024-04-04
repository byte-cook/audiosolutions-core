package de.kobich.audiosolutions.core.service.playlist.repository;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="playlist", uniqueConstraints=@UniqueConstraint(name="UK_playlist", columnNames={"name", "system"}))
@NamedEntityGraph(
		name = Playlist.GRAPH,
		includeAllAttributes = true,
		attributeNodes = { 
			@NamedAttributeNode(value = "folders", subgraph = "graph.playlist.folders")
		}, 
		subgraphs = { 
			@NamedSubgraph(name = "graph.playlist.folders", type=PlaylistFolder.class, 
				attributeNodes = { @NamedAttributeNode(value = "files", subgraph = "graph.playlist.files") })
		})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Playlist {
	public static final String GRAPH = "graph.playlist";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name="name", unique=false, nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@Column(name="system", unique=false, nullable=true)
	private boolean system;
	
	@OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="playlist_id", nullable=false)
	private Set<PlaylistFolder> folders;

	public Playlist(String name, boolean system) {
		this.name = name;
		this.system = system;
		this.folders = new HashSet<>();
	}
	
}
