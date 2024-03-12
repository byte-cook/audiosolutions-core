package de.kobich.audiosolutions.core.service.persist.domain;

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

/**
 * Represents an artist.
 */
@Entity
@Table(name="artist", uniqueConstraints=@UniqueConstraint(name="UK_artist", columnNames={"name"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Artist {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name="name", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@Column(name="description", nullable=true)
	private String description;

	/**
	 * Constructor
	 * @param name the name
	 */
	public Artist(String name) {
		this.setName(name);
	}

}
