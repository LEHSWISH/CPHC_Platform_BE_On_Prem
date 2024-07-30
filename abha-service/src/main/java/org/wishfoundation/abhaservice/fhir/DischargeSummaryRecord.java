package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating FHIR resources related to a discharge summary.
 */
public class DischargeSummaryRecord {

    /**
     * This method populates a FHIR Composition resource for a discharge summary.
     *
     * @param fhirAdditionalDetails Additional details required to populate the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populateDischargeSummaryCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {

        // Generate unique identifiers for various FHIR resources
        String compositionUUID = Helper.getUniqueIdentifier();
        String patientUUID = Helper.getUniqueIdentifier();
        String organizationId = Helper.getUniqueIdentifier();
        String documentId = Helper.getUniqueIdentifier();
        String encounterId = Helper.getUniqueIdentifier();

        // Create a new Composition resource
        Composition composition = new Composition();
        composition.setId(compositionUUID);

        // Set metadata for the Composition
        Meta meta = composition.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DischargeSummaryRecord");

        // Set the type of composition
        composition.setType(new CodeableConcept(new Coding("http://snomed.info/sct", "373942005", "Discharge summary")).setText("Discharge summary"));
        composition.setStatus(Composition.CompositionStatus.FINAL);

        // Set other relevant details for the Composition
        composition.setDateElement(DateTimeType.now());
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));
        composition.setEncounter(new Reference().setReference(encounterId));
        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));

        composition.setTitle("Discharge Summary");

        // Add a section to the Composition
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "373942005", "Discharge summary")))
                .addEntry(new Reference().setReference(documentId).setType("DocumentReference").setDisplay(fhirAdditionalDetails.getFileName()));
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).encounterId(encounterId).organizationId(organizationId).documentId(documentId).composition(composition).build();
    }

    /**
     * This method populates a FHIR Bundle resource for a discharge summary.
     *
     * @param fhirAdditionalDetails Additional details required to populate the resource.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populateDischargeSummaryBundle(FHIRAdditionalDetails fhirAdditionalDetails)
    {
        // Create a new Bundle resource
        Bundle dischargeSummaryBundle = new Bundle();

        // Set logical id of this artifact
        dischargeSummaryBundle.setId(UUID.randomUUID().toString());

        // Set metadata for the Bundle
        Meta meta = dischargeSummaryBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set version-independent identifier for the Bundle
        Identifier identifier = dischargeSummaryBundle.getIdentifier();
        identifier.setSystem("http://hip.in");
        identifier.setValue(UUID.randomUUID().toString());

        // Set Bundle Type
        dischargeSummaryBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set Timestamp
        dischargeSummaryBundle.setTimestampElement(InstantType.now());

        // Add resources entries for bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = dischargeSummaryBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate the Composition resource and add it to the Bundle
        FhirBundleRequest fhirBundleRequest =  populateDischargeSummaryCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        // Populate other resources and add them to the Bundle
        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        bundleEntry3.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry3.setResource(ResourcePopulator.populateDocumentReferenceResource(fhirBundleRequest.getDocumentId(),fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        bundleEntry4.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry4.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry5 = new Bundle.BundleEntryComponent();
        bundleEntry5.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry5.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry6 = new Bundle.BundleEntryComponent();
        bundleEntry6.setFullUrl(fhirBundleRequest.getEncounterId());
        bundleEntry6.setResource(ResourcePopulator.populateEncounterResource(fhirBundleRequest.getEncounterId()));

        // Add the BundleEntryComponents to the Bundle
        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);
        listBundleEntries.add(bundleEntry5);
        listBundleEntries.add(bundleEntry6);

        // Return the populated Bundle
        return dischargeSummaryBundle;
    }
}
