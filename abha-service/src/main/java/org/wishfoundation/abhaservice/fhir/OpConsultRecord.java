package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;
import org.wishfoundation.abhaservice.request.fhir.FhirBundleRequest;
import org.wishfoundation.abhaservice.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for populating the FHIR resources related to an OP Consult Note.
 */
public class OpConsultRecord {

    /**
     * This method populates a Composition resource for an OP Consult Note.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resource.
     * @return A FhirBundleRequest object containing the populated Composition resource.
     */
    public static FhirBundleRequest populateOPConsultNoteCompositionResource(FHIRAdditionalDetails fhirAdditionalDetails) {

        // Generate unique identifiers for the resources
        String compositionUUID = Helper.getUniqueIdentifier();
        String patientUUID = Helper.getUniqueIdentifier();
        String organizationId = Helper.getUniqueIdentifier();
        String documentId = Helper.getUniqueIdentifier();
        String encounterId = Helper.getUniqueIdentifier();

        // Create a new Composition resource
        Composition composition = new Composition();
        composition.setId(compositionUUID);

        // Set metadata about the resource - Version Id, Lastupdated Date, Profile
        Meta meta = composition.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/OPConsultRecord");

        // Kind of composition ("Clinical consultation report")
        composition.setType(new CodeableConcept(new Coding("http://snomed.info/sct",
                "371530004", "Clinical consultation report")).setText("Clinical Consultation report"));
        composition.setStatus(Composition.CompositionStatus.FINAL);

        composition.setDateElement(DateTimeType.now());
        composition.setSubject(new Reference().setReference(patientUUID).setDisplay(fhirAdditionalDetails.getPatientName()));
        composition.setEncounter(new Reference().setReference(encounterId));

        composition.addAuthor(new Reference().setReference(organizationId).setDisplay(fhirAdditionalDetails.getOrganizationName()));

        composition.setTitle("Consultation Report");
        List<Composition.SectionComponent> sectionList = new ArrayList<>();
        Composition.SectionComponent section = new Composition.SectionComponent();
        section.setTitle("Document Reference");
        section.setCode(
                        new CodeableConcept(new Coding("http://snomed.info/sct", "371530004", "Clinical consultation report")))
                .addEntry(new Reference().setReference(documentId).setType("DocumentReference").setDisplay(fhirAdditionalDetails.getFileName()));
        sectionList.add(section);
        composition.addSection(section);

        // Return the populated FhirBundleRequest object
        return FhirBundleRequest.builder().compositionId(compositionUUID).patientId(patientUUID).organizationId(organizationId).encounterId(encounterId).documentId(documentId).composition(composition).build();
    }

    /**
     * This method populates a Bundle resource containing all the required resources for an OP Consult Note.
     *
     * @param fhirAdditionalDetails Additional details required for populating the resources.
     * @return A Bundle object containing the populated resources.
     */
    public static Bundle populateOPConsultNoteBundle(FHIRAdditionalDetails fhirAdditionalDetails)
    {
        // Create a new Bundle resource
        Bundle opCounsultNoteBundle = new Bundle();

        // Set logical id of this artifact
        opCounsultNoteBundle.setId(UUID.randomUUID().toString());

        // Set metadata about the resource - Version Id, Lastupdated Date, Profile
        Meta meta = opCounsultNoteBundle.getMeta();
        meta.setVersionId("1");
        meta.setLastUpdatedElement(InstantType.now());
        meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");

        // Set version-independent identifier for the Bundle
        Identifier identifier = opCounsultNoteBundle.getIdentifier();
        identifier.setValue(UUID.randomUUID().toString());
        identifier.setSystem("http://hip.in");

        // Set Bundle Type
        opCounsultNoteBundle.setType(Bundle.BundleType.DOCUMENT);

        // Set Timestamp
        opCounsultNoteBundle.setTimestampElement(InstantType.now());

        // Add resources entries for bundle with Full URL
        List<Bundle.BundleEntryComponent> listBundleEntries = opCounsultNoteBundle.getEntry();

        Bundle.BundleEntryComponent bundleEntry1 = new Bundle.BundleEntryComponent();

        // Populate the Composition resource and add it to the Bundle
        FhirBundleRequest fhirBundleRequest =  populateOPConsultNoteCompositionResource(fhirAdditionalDetails);
        bundleEntry1.setFullUrl(fhirBundleRequest.getCompositionId());
        bundleEntry1.setResource(fhirBundleRequest.getComposition());

        // Populate other required resources and add them to the Bundle
        Bundle.BundleEntryComponent bundleEntry2 = new Bundle.BundleEntryComponent();
        bundleEntry2.setFullUrl(fhirBundleRequest.getEncounterId());
        bundleEntry2.setResource(ResourcePopulator.populateEncounterResource(fhirBundleRequest.getEncounterId()));

        Bundle.BundleEntryComponent bundleEntry3 = new Bundle.BundleEntryComponent();
        bundleEntry3.setFullUrl(fhirBundleRequest.getDocumentId());
        bundleEntry3.setResource(ResourcePopulator.populateDocumentReferenceResource(fhirBundleRequest.getDocumentId(),fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry4 = new Bundle.BundleEntryComponent();
        bundleEntry4.setFullUrl(fhirBundleRequest.getPatientId());
        bundleEntry4.setResource(ResourcePopulator.populatePatientResource(fhirBundleRequest.getPatientId(), fhirAdditionalDetails));

        Bundle.BundleEntryComponent bundleEntry5 = new Bundle.BundleEntryComponent();
        bundleEntry5.setFullUrl(fhirBundleRequest.getOrganizationId());
        bundleEntry5.setResource(ResourcePopulator.populateOrganizationResource(fhirBundleRequest.getOrganizationId(), fhirAdditionalDetails));

        listBundleEntries.add(bundleEntry1);
        listBundleEntries.add(bundleEntry2);
        listBundleEntries.add(bundleEntry3);
        listBundleEntries.add(bundleEntry4);
        listBundleEntries.add(bundleEntry5);

        // Return the populated Bundle object
        return opCounsultNoteBundle;
    }
}
