package org.wishfoundation.healthservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

/**
 * Represents a District entity in the application.
 * This class is annotated with Hibernate annotations to map it to a database table.
 * It implements the BaseEntity interface to inherit common properties.
 *
 */
@Getter
@Setter
@Entity
@Table(name = "district")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "healthFacility", "state" })
public class District implements BaseEntity {

	@Serial
	private static final long serialVersionUID = -2415329498659088657L;

    /**
     * The unique identifier for the district.
     * It is annotated with @Id and @Column to map it to the database column.
     */
	@Id
	@Column(name = "district_code", length = 5)
	private String districtCode;

    /**
     * The state code associated with the district.
     * It is annotated with @Column to map it to the database column.
     */
	// TODO : need to create index
	@Column(name = "state_code", length = 5)
	private String stateCode;

    /**
     * The state associated with the district.
     * It is annotated with @ManyToOne, @JoinColumn, @MapsId, and @JsonBackReference to establish a relationship with the State entity.
     * The fetch type is set to LAZY to improve performance.
     */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "state_code", referencedColumnName = "state_code")
	@MapsId("stateCode")
	@JsonBackReference(value = "state-district")
	private State state;

    /**
     * The list of health facilities associated with the district.
     * It is annotated with @OneToMany and @JsonBackReference to establish a relationship with the HealthFacility entity.
     * The fetch type is set to LAZY to improve performance.
     */
	@OneToMany(mappedBy = "district", fetch = FetchType.LAZY)
	@JsonBackReference(value = "district-healthFacility")
	private List<HealthFacility> healthFacility;

    /**
     * The name of the district.
     * It is annotated with @Column to map it to the database column.
     */
	@Column(name = "district_name", length = 50)
	private String districtName;

}