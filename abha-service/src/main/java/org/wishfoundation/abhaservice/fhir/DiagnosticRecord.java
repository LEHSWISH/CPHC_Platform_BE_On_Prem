package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating FHIR resources related to Diagnostic Report - Lab.
 */
public class DiagnosticRecord {

    /**
     * This method populates a Composition resource for Diagnostic Report - Lab.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populateDiagnosticReportRecordLabCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {

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
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DiagnosticReportRecord");
        composition.setLanguage("en-IN");

        // Set the kind of composition
        CodeableConcept type = composition.getType();
        type.addCoding(new Coding("http://snomed.info/sct", "721981007", "Diagnostic studies report"));
        type.setText("Diagnostic Report- Lab");

        // Set the status of the composition
        composition.setStatus(Composition.CompositionStatus.FINAL);

        // Set the date of the composition
        composition.setDateElement(DateTimeType.now());

        // Set the subject of the composition
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));

        // Add an author to the composition
        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));

        // Set the title of the composition
        composition.setTitle("Diagnostic Report- Lab");

        // Add a section to the composition
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setTitle("Hematology report");
        section.setCode(new CodeableConcept(new Coding("http://snomed.info/sct", "4321000179101", "Hematology report")));
        section.addEntry(new Reference().setReference(documentId).setType("DocumentReference").setDisplay(fhirAdditionalDetails.getFileName()));
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).organizationId(organizationId).documentId(documentId).composition(composition).build();
    }

    /**
     * This method populates a Bundle resource containing the Diagnostic Report - Lab resources.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resources.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populateDiagnosticReportLabBundle(FHIRAdditionalDetails fhirAdditionalDetails) {

        // Create a new Bundle resource
        Bundle diagnosticReportBundle = new Bundle();

        // Set the logical id of the bundle
        diagnosticReportBundle.setId(UUID.randomUUID().toString());

        // Set metadata for the bundle
        Meta meta = diagnosticReportBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set a version-independent identifier for the bundle
        Identifier identifier = diagnosticReportBundle.getIdentifier();
        identifier.setSystem("http://hip.in");
        identifier.setValue(UUID.randomUUID().toString());

        // Set the type of the bundle
        diagnosticReportBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set the timestamp of the bundle
        diagnosticReportBundle.setTimestampElement(InstantType.now());

        // Add resources entries for the bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = diagnosticReportBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate the resources using the helper method
        FhirBundleRequest fhirBundleRequest =  populateDiagnosticReportRecordLabCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        bundleEntry3.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry3.setResource(ResourcePopulator.populateDocumentReferenceResource(fhirBundleRequest.getDocumentId(),fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        bundleEntry4.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry4.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry5 = new Bundle.BundleEntryComponent();
        bundleEntry5.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry5.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        // Add the bundle entries to the bundle
        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);
        listBundleEntries.add(bundleEntry5);

        // Return the populated Bundle object
        return diagnosticReportBundle;
    }
}
