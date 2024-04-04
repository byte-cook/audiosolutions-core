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
@Table(name="playlistfolder", uniqueConstraints=@UniqueConstraint(name="UK_playlistfolder", columnNames={"path", "playlist_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class PlaylistFolder {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	private Long id;

	@Column(name="path", unique=false, nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String path;
	
//	@ManyToOne(fetch=FetchType.EAGER)
//	@JoinColumn(name="playlist_id", foreignKey=@ForeignKey(name="FK_folder_playlist"), nullable=false)
//	@EqualsAndHashCode.Include
//	@ToString.Include
//	@OnDelete(action = OnDeleteAction.CASCADE)
//	private Playlist playlist;
	
	@OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="playlistfolder_id", nullable=false)
	private Set<PlaylistFile> files;
	
	public PlaylistFolder(String path) {
		this.path = path;
		this.files = new HashSet<>();
	}
	
	
}
