package org.wishfoundation.userservice.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.userservice.enums.MedicalDocumentType;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsPathResponse{
	private String id;
	private String fileName;
	private String filePath;
	private Instant createdOn;
	private Instant updatedOn;
	private MedicalDocumentType medicalDocumentType;
	private String hospitalLabName;
	private String visitPurpose;
	private String careContextId;
}
