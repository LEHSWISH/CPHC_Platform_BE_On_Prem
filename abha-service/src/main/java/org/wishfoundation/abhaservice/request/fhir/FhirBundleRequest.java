package org.wishfoundation.abhaservice.request.fhir;

import lombok.Builder;
import lombok.Getter;
import org.hl7.fhir.r4.model.Composition;

@Getter
@Builder(toBuilder = true)
public class FhirBundleRequest {
    String patientId;
    String organizationId;
    String compositionId;
    String documentId;

    String encounterId;

    Composition composition;

}
