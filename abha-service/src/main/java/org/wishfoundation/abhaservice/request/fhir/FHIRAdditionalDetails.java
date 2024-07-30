package org.wishfoundation.abhaservice.request.fhir;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;

@Getter
@Builder(toBuilder = true)
@Setter
public class FHIRAdditionalDetails {
    String base64Content;
    String contentType;
    String fileName;
    String patientName;
    String organizationName;
    MedicalDocumentType heathInformationType;
    String gender;
    String phoneNumber;
    String dateOfBirth;
}
