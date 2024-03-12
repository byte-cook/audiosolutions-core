package de.kobich.audiosolutions.core.service.persist.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a medium (e.g. a compact disk).
 */
@Entity
@Table(name="medium", uniqueConstraints=@UniqueConstraint(name="UK_medium", columnNames={"name"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Medium {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;

	@Column(name="name", unique=true, nullable=false)
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@Column(name="lent", nullable=false)
	private boolean lent;
	
	@Column(name="borrower", nullable=true)
	private String borrower;
	
	@Column(name="borrowing_date", nullable=true)
	@Temporal(TemporalType.DATE)
	private Date borrowingDate;
	
	@Column(name="copyavailable", nullable=false)
	private boolean copyAvailable;
	
	/**
	 * Constructor
	 * @param name the name
	 */
	public Medium(String name) {
		this.setName(name);
	}

}
