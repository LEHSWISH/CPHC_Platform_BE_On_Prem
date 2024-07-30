package org.wishfoundation.chardhamcore.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.chardhamcore.enums.DocumentType;


import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "yatri_pulse_users", uniqueConstraints = @UniqueConstraint(columnNames = { "user_name", "phone_number" }))
public class YatriPulseUsers extends AuditableEntity {

	/**
	 *
	 */
	private static final long serialVersionUID = -4630428315685866182L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "user_name", unique = true)
	private String userName;

	@Column(name = "password")
	private String password;

	@Column(name = "phone_number", length = 15)
	private String phoneNumber;

	@Column(name = "license_agreement")
	private boolean licenseAgreement;

	@Column(name = "license_agreement_time")
	private java.time.Instant licenseAgreementTime;

}
