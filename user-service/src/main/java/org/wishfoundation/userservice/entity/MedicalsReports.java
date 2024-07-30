package org.wishfoundation.userservice.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medicals_reports")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsersDetails" })
public class MedicalsReports extends AuditableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3527308362354822531L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "heart_disease")
	private boolean heartDisease;

	@Column(name = "hypertension")
	private boolean hypertension;

	@Column(name = "respiratory_disease_or_asthma")
	private boolean respiratoryDiseaseOrAsthma;

	@Column(name = "diabetes_mellitus")
	private boolean diabetesMellitus;

	@Column(name = "tuberculosis")
	private boolean tuberculosis;

	@Column(name = "epilepsy_or_any_neurological_disorder")
	private boolean epilepsyOrAnyNeurologicalDisorder;

	@Column(name = "kidney_or_urinary_disorder")
	private boolean kidneyOrUrinaryDisorder;

	@Column(name = "cancer")
	private boolean cancer;

	@Column(name = "migraine_or_persistent_headache")
	private boolean migraineOrPersistentHeadache;

	@Column(name = "any_allergies")
	private boolean anyAllergies;

	@Column(name = "disorder_of_the_joints_or_muscles_arthritis_gout")
	private boolean disorderOfTheJointsOrMusclesArthritisGout;

	@Column(name = "any_major_surgery")
	private boolean anyMajorSurgery;

	@Column(name = "none_of_the_above")
	private boolean noneOfTheAbove;

	@Column(name = "yatri_pulse_user_id",length = 50)
	private UUID yatriPulseUserId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
	@MapsId("yatriPulseUserId")
	private YatriPulseUsers yatriPulseUsersDetails;

}
