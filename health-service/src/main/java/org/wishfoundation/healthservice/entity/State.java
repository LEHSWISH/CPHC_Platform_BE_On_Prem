package org.wishfoundation.healthservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

/**
 * This class represents a State entity in the application.
 * It is annotated with Hibernate annotations to map it to a database table.
 * It implements the BaseEntity interface, which provides common properties and methods.
 * The class uses Lombok annotations to generate getters, setters, constructors, and other boilerplate code.
 * The class is annotated with @JsonIgnoreProperties to ignore certain fields during JSON serialization.
 */
@Getter
@Setter
@Entity
@Table(name = "state")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "district", "healthFacility" })
public class State implements BaseEntity {

    /**
     * A static final long variable to hold the serialVersionUID.
     * It is used for serialization and deserialization of objects.
     */
	@Serial
	private static final long serialVersionUID = 1579882455529525487L;

    /**
     * A String variable to hold the state code.
     * It is annotated with @Id and @Column to map it to a database column.
     */
	@Id
	@Column(name = "state_code", length = 5)
	private String stateCode;

    /**
     * A String variable to hold the state name.
     * It is annotated with @Column to map it to a database column.
     */
	@Column(name = "state_name", length = 50)
	private String stateName;

    /**
     * A String variable to hold the country name.
     * It is annotated with @Column to map it to a database column.
     */
	@Column(name = "country_name", length = 50)
	private String countryName;

    /**
     * A String variable to hold the country code.
     * It is annotated with @Column to map it to a database column.
     */
	@Column(name = "country_code", length = 5)
	private String countryCode;

    /**
     * A List of District objects to hold the districts associated with this state.
     * It is annotated with @OneToMany and @JsonManagedReference to establish a bidirectional relationship.
     * The fetch type is set to LAZY to defer loading of the districts until they are explicitly accessed.
     */
	@OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
	@JsonManagedReference(value = "state-district")
	private List<District> district;

    /**
     * A List of HealthFacility objects to hold the health facilities associated with this state.
     * It is annotated with @OneToMany and @JsonManagedReference to establish a bidirectional relationship.
     * The fetch type is set to LAZY to defer loading of the health facilities until they are explicitly accessed.
     */
	@OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
	@JsonManagedReference(value = "state-healthFacility")
	private List<HealthFacility> healthFacility;
}
