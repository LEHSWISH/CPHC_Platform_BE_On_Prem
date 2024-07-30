package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.MedicalDocumentType;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "documents_path", uniqueConstraints = @UniqueConstraint(columnNames = { "file_name", "file_path", "yatri_pulse_user_id" }))
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsers"})
public class DocumentsPath extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	@Column(name = "file_name", length = 50)
	private String fileName;

	@Column(name = "file_path", length = 255)
	private String filePath;

	@Column(name = "yatri_pulse_user_id", length = 50)
	private UUID yatriPulseUserId;

	@Column(name = "medical_document_type", length = 50)
	@Enumerated(EnumType.STRING)
	private MedicalDocumentType medicalDocumentType;

	@Column(name = "hospital_lab_name", length = 100)
	private String hospitalLabName;

	@Column(name = "visit_purpose", length = 100)
	private String visitPurpose;

	@Column(name = "care_context_id", length = 100)
	private String careContextId;

	@JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("yatriPulseUserId")
	@JsonBackReference("yatriPulseUser-documentsPath")
	private YatriPulseUsers yatriPulseUsers;

}
