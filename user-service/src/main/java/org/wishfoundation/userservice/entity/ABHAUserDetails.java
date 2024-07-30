package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "abha_user_details")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "yatriPulseUsersDetails"})
public class ABHAUserDetails extends AuditableEntity {

    /**
     *
     */
    private static final long serialVersionUID = -8753427500457267658L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "abha_number", unique = true)
    private String abhaNumber;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;

    @Column(name = "gender", length = 15)
    private String gender;

    @Column(name = "image_path", length = 100)
    private String imagePath;

    @Column(name = "mobile_number", length = 15)
    private String phoneNumber;

    @Column(name = "email_id", length = 254)
    private String emailId;

    @Column(name = "phr_address")
    private List<String> phrAddress;

    @Column(name = "address")
    private String address;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @Column(name = "state_code", length = 20)
    private String stateCode;

    @Column(name = "district_Name", length = 50)
    private String districtName;

    @Column(name = "state_Name", length = 20)
    private String stateName;

    @Column(name = "pin_code", length = 8)
    private String pinCode;
    @Column(name = "abha_type", length = 50)
    private String abhaType;
    @Column(name = "abha_status", length = 50)
    private String abhaStatus;

    @Column(name = "yatri_pulse_user_id", length = 50)
    private UUID yatriPulseUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
    @MapsId("yatriPulseUserId")
    private YatriPulseUsers yatriPulseUsersDetails;

    //Todo: Handle this full name
	public String getFullName() {
		String m = StringUtils.hasLength(this.middleName) ? " " + this.middleName : "";
		String l = StringUtils.hasLength(this.lastName) ? " " + this.lastName : "";
		return this.firstName + m + l;
	}

	public void setFullName() {
		String m = StringUtils.hasLength(this.middleName) ? " " + this.middleName : "";
		String l = StringUtils.hasLength(this.lastName) ? " " + this.lastName : "";
		this.fullName = this.firstName + m + l;
	}
}