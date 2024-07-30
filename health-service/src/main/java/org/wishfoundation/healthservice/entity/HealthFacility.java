package org.wishfoundation.healthservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * Represents a health facility entity in the system.
 * This entity is mapped to the 'health_facility' table in the database.
 * It includes fields for state, district, facility details, and ABDM (Automated Beneficiary Data Management) status.
 *
 *
 * @version 1.0
 */
@Getter
@Setter
@Entity
@Table(name = "health_facility")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "district", "state" })
public class HealthFacility implements BaseEntity {

	@Serial
	private static final long serialVersionUID = -7279076773098678022L;

    /**
     * Unique identifier for the health facility.
     */
	@Id
	private String id;

    /**
     * State code associated with the health facility.
     */
	@Column(name = "state_code", length = 5)
	private String stateCode;

    /**
     * District code associated with the health facility.
     */
	@Column(name = "district_code", length = 5)
	private String districtCode;

    /**
     * District entity associated with the health facility.
     * This field is fetched lazily to optimize performance.
     * The relationship is defined using the 'district_code' column in the 'health_facility' table.
     */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "district_code", referencedColumnName = "district_code")
	@MapsId("districtCode")
	@JsonBackReference(value = "district-healthFacility")
	private District district;

    /**
     * State entity associated with the health facility.
     * This field is fetched lazily to optimize performance.
     * The relationship is defined using the 'state_code' column in the 'health_facility' table.
     */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "state_code", referencedColumnName = "state_code")
	@MapsId("stateCode")
	@JsonBackReference(value = "state-healthFacility")
	private State state;

    /**
     * Name of the health facility.
     */
	@Column(name = "facility_name")
	private String facilityName;

    /**
     * Unique identifier for the health facility within its state.
     */
	@Column(name = "facility_id")
	private String facilityId;

    /**
     * Type of the health facility (e.g., hospital, clinic).
     */
	@Column(name = "facility_type")
	private String facilityType;

    /**
     * Ownership of the health facility (e.g., government, private).
     */
	@Column(name = "ownership")
	private String ownership;

    /**
     * Address of the health facility.
     */
	@Column(name = "address")
	private String address;

    /**
     * Current status of the health facility (e.g., active, closed).
     */
	@Column(name = "facility_status")
	private String facilityStatus;

    /**
     * Indicates whether Automated Beneficiary Data Management (ABDM) is enabled for the health facility.
     * This field is marked as non-nullable.
     */
	@Column(name = "abdm_enabled", nullable = false)
	private boolean abdmEnabled;
}
