package org.wishfoundation.userservice.entity;

import java.util.UUID;

import jakarta.persistence.*;
import org.wishfoundation.userservice.enums.Gender;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "yatri_details")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsersDetails" })
public class YatriDetails extends AuditableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9038888688407307531L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;
	@Column(name="full_name",length= 50)
	private String fullName;

	@Column(name = "first_name", length = 50)
	private String firstName;

	@Column(name = "last_name", length = 50)
	private String lastName;

	@Column(name = "email_id", length = 254)
	private String emailId;

	@Column(name = "phone_number", length = 254)
	private String phoneNumber;

	@Column(name = "gender", length = 15)
	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Column(name = "data_of_birth", length = 20)
	private String dateOfBirth;

	@Column(name = "tour_start_date", length = 20)
	private String tourStartDate;

	@Column(name = "tour_end_date", length = 20)
	private String tourEndDate;

	@Column(name = "tour_duration", length = 5)
	private int tourDuration;

	@Column(name = "age", length = 5)
	private int age;

	@Column(name = "address" , length = 160)
	private String address;

	@Column(name = "pin_code" , length = 8)
	private String pinCode;

	@Column(name = "state" , length = 50)
	private String state;
	@Column(name = "district" , length = 50)
	private String district;

	@Column(name = "yatri_pulse_user_id", length = 50)
	private UUID yatriPulseUserId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
	@MapsId("yatriPulseUserId")
	private YatriPulseUsers yatriPulseUsersDetails;

}
