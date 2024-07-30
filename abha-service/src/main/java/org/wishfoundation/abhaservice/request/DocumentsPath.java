package org.wishfoundation.abhaservice.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;

import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentsPath {
	private UUID id;
	private String fileName;
	private String filePath;
	private UUID yatriPulseUserId;
	private MedicalDocumentType medicalDocumentType;
	private String hospitalLabName;
	private String visitPurpose;
	private String careContextId;
}
