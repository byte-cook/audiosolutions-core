package de.kobich.audiosolutions.core.service.playlist.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="playlistfile", uniqueConstraints=@UniqueConstraint(name="UK_playlistfile", columnNames={"name", "file_path", "playlistfolder_id"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class PlaylistFile {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	private Long id;

	@Column(name="name", unique=false, nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@Column(name = "file_path", unique=false, nullable=false)
	@EqualsAndHashCode.Include
	private String filePath;
	
	@Column(name = "sort_order", unique=false, nullable=false)
	private long sortOrder;
	
	public PlaylistFile(String name, String filePath) {
		this.name = name;
		this.filePath = filePath;
	}
	
}
