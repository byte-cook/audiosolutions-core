package de.kobich.audiosolutions.core.service.persist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a genre.
 */
@Entity
//@Table(name="genre", uniqueConstraints=@UniqueConstraint(name="UK_genre", columnNames={"name"}))
@Table(name="genre")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Genre {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private long id;
	
	@Column(name="name", unique=true, nullable=false, insertable=true, updatable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;

	public Genre(String name) {
		this.name = name;
	}
}
