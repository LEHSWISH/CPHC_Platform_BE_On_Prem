package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating FHIR resources related to a prescription record.
 */
public class PrescriptionRecord {

    /**
     * This method populates a FHIR Composition resource for a prescription record.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populatePrescriptionCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {
        // Generate unique identifiers for resources
        String compositionUUID = Helper.getUniqueIdentifier();
        String patientUUID = Helper.getUniqueIdentifier();
        String organizationId = Helper.getUniqueIdentifier();
        String binaryRequestUUID = Helper.getUniqueIdentifier();

        // Create a new Composition resource
        Composition composition = new Composition();
        composition.setId(compositionUUID);

        // Set metadata for the Composition
        Meta meta = composition.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/PrescriptionRecord");

        // Set status, type, subject, date, author, and title of the Composition
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.setType(new CodeableConcept(new Coding("http://snomed.info/sct", "440545006", "Prescription record")));
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));
        composition.setDateElement(DateTimeType.now());
        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));
        composition.setTitle("Prescription record");

        // Create a reference to the Binary resource
        Reference reference = new Reference();
        reference.setReference(binaryRequestUUID).setDisplay(fhirAdditionalDetails.getFileName());
        reference.setType("Binary");

        // Add a section to the Composition
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setTitle("Prescription record");
        section.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "440545006", "Prescription record"))).addEntry(reference);
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).organizationId(organizationId).documentId(binaryRequestUUID).composition(composition).build();
    }

    /**
     * This method populates a FHIR Bundle resource containing the prescription record resources.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resources.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populatePrescriptionBundle(FHIRAdditionalDetails fhirAdditionalDetails) {
        // Create a new Bundle resource
        Bundle prescriptionBundle = new Bundle();

        // Set logical id of the Bundle
        prescriptionBundle.setId(UUID.randomUUID().toString());

        // Set metadata for the Bundle
        Meta meta = prescriptionBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set version-independent identifier for the Bundle
        Identifier identifier = prescriptionBundle.getIdentifier();
        identifier.setValue(UUID.randomUUID().toString());
        identifier.setSystem("http://hip.in");

        // Set Bundle type
        prescriptionBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set timestamp for the Bundle
        prescriptionBundle.setTimestampElement(InstantType.now());

        // Add resources entries for the Bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = prescriptionBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate the Composition resource using the populatePrescriptionCompositionResource method
        FhirBundleRequest fhirBundleRequest = populatePrescriptionCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        Bundle.BundleEntryComponent bundleEntry2 = new Bundle.BundleEntryComponent();
        bundleEntry2.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry2.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        bundleEntry3.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry3.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        bundleEntry4.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry4.setResource(ResourcePopulator.populateBinaryResource(fhirBundleRequest.getDocumentId(), fhirAdditionalDetails));

        // Add the BundleEntryComponents to the Bundle
        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry2);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);

        // Return the populated Bundle
        return prescriptionBundle;
    }
}
