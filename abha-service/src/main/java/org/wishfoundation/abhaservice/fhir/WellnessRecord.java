package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating FHIR resources related to Wellness Record.
 */
public class WellnessRecord {

    /**
     * This method populates a Composition resource for Wellness Record.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populateWellnessRecordCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {

        // Generate unique identifiers for resources
        String compositionUUID = Helper.getUniqueIdentifier();
        String patientUUID = Helper.getUniqueIdentifier();
        String organizationId = Helper.getUniqueIdentifier();
        String documentId = Helper.getUniqueIdentifier();

        // Create a new Composition resource
        Composition composition = new Composition();
        composition.setId(compositionUUID);

        // Set metadata for the Composition
        Meta meta = composition.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/WellnessRecord");
        composition.setLanguage("en-IN");

        // Set the type of composition
        composition.setType(new CodeableConcept(new Coding("http://snomed.info/sct", "419891008", "Record artifact")).setText("Record artifact"));
        composition.setStatus(Composition.CompositionStatus.FINAL);

        // Set other relevant details for the Composition
        composition.setDateElement(DateTimeType.now());
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));
        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));

        composition.setTitle("Wellness Record");

        // Add a section to the Composition
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setTitle("Document Reference");
        section.addEntry(new Reference().setReference(documentId).setType("DocumentReference").setDisplay("FN"));
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).organizationId(organizationId).documentId(documentId).composition(composition).build();
    }

    /**
     * This method populates a Bundle resource containing Wellness Record related resources.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resources.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populateWellnessRecordBundle(FHIRAdditionalDetails fhirAdditionalDetails)
    {
        // Create a new Bundle resource
        Bundle wellnessRecordBundle = new Bundle();

        // Set logical id of the Bundle
        wellnessRecordBundle.setId(UUID.randomUUID().toString());

        // Set metadata for the Bundle
        Meta meta = wellnessRecordBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set version-independent identifier for the Bundle
        Identifier identifier = wellnessRecordBundle.getIdentifier();
        identifier.setSystem("http://hip.in");
        identifier.setValue(UUID.randomUUID().toString());

        // Set the type of Bundle
        wellnessRecordBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set the timestamp for the Bundle
        wellnessRecordBundle.setTimestampElement(InstantType.now());

        // Add resources entries for the Bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = wellnessRecordBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate the Composition resource and add it to the Bundle
        FhirBundleRequest fhirBundleRequest =  populateWellnessRecordCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        // Populate the DocumentReference resource and add it to the Bundle
        bundleEntry3.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry3.setResource(ResourcePopulator.populateDocumentReferenceResource(fhirBundleRequest.getDocumentId(),fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        // Populate the Patient resource and add it to the Bundle
        bundleEntry4.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry4.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry5 = new Bundle.BundleEntryComponent();
        // Populate the Organization resource and add it to the Bundle
        bundleEntry5.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry5.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        // Add the BundleEntryComponents to the Bundle
        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);
        listBundleEntries.add(bundleEntry5);

        // Return the populated Bundle
        return wellnessRecordBundle;
    }
}
