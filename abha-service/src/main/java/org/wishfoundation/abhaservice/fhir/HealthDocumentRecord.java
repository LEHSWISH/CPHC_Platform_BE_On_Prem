package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating FHIR resources related to Health Document Record.
 */
public class HealthDocumentRecord {

    /**
     * This method populates a FHIR Composition resource for Health Document Record.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populateHealthDocumentRecordCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {

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
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/HealthDocumentRecord");

        // Set type and status of the Composition
        composition.setType(new CodeableConcept(new Coding("http://snomed.info/sct", "419891008", "Record artifact")).setText("Record artifact"));
        composition.setStatus(Composition.CompositionStatus.FINAL);

        // Set date and subject of the Composition
        composition.setDateElement(DateTimeType.now());
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));
        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));

        // Set title of the Composition
        composition.setTitle("Health Document");

        // Add a section to the Composition
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setTitle("Health Document");
        section.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "419891008", "Record artifact")))
                .addEntry(new Reference().setReference(documentId).setType("DocumentReference").setDisplay(fhirAdditionalDetails.getFileName()));
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).organizationId(organizationId).documentId(documentId).composition(composition).build();
    }

    /**
     * This method populates a FHIR Bundle resource for Health Document Record.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populateHealthDocumentRecordBundle(FHIRAdditionalDetails fhirAdditionalDetails)
    {
        // Create a new Bundle resource
        Bundle healthDocumentRecordBundle = new Bundle();

        // Set unique identifier for the Bundle
        healthDocumentRecordBundle.setId(UUID.randomUUID().toString());

        // Set metadata for the Bundle
        Meta meta = healthDocumentRecordBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set version-independent identifier for the Composition
        Identifier identifier = healthDocumentRecordBundle.getIdentifier();
        identifier.setSystem("http://hip.in");
        identifier.setValue(UUID.randomUUID().toString());

        // Set Bundle Type
        healthDocumentRecordBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set Timestamp
        healthDocumentRecordBundle.setTimestampElement(InstantType.now());

        // Add resources entries for bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = healthDocumentRecordBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate Composition resource and add it to the Bundle
        FhirBundleRequest fhirBundleRequest =  populateHealthDocumentRecordCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        // Populate DocumentReference resource and add it to the Bundle
        bundleEntry3.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry3.setResource(ResourcePopulator.populateDocumentReferenceResource(fhirBundleRequest.getDocumentId(),fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        // Populate Patient resource and add it to the Bundle
        bundleEntry4.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry4.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry5 = new Bundle.BundleEntryComponent();
        // Populate Organization resource and add it to the Bundle
        bundleEntry5.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry5.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        // Add BundleEntryComponents to the Bundle
        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);
        listBundleEntries.add(bundleEntry5);

        // Return the populated Bundle
        return healthDocumentRecordBundle;
    }
}
