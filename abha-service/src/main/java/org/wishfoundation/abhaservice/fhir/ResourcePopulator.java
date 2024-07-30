package org.wishfoundation.abhaservice.fhir;

import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceContentComponent;
import org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.wishfoundation.abhaservice.request.fhir.FHIRAdditionalDetails;

/**
 * The FhirResourcePopulator class populates all the FHIR resources
 */
public class ResourcePopulator {
	public static Patient populatePatientResource(String patientId, FHIRAdditionalDetails fhirAdditionalDetails) {
		Patient patient = new Patient();
		patient.setId(patientId);
		patient.getMeta().setVersionId("1").setLastUpdatedElement(InstantType.now())
				.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Patient");
		patient.addIdentifier()
				.setType(new CodeableConcept(
						new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "MR", "Medical record number")))
				.setSystem("https://healthid.ndhm.gov.in").setValue("22-7225-4829-5255");
		patient.addName().setText(fhirAdditionalDetails.getPatientName());
		//Todo: Telecom optional
		patient.addTelecom().setSystem(ContactPointSystem.PHONE).setValue(fhirAdditionalDetails.getPhoneNumber()).setUse(ContactPointUse.HOME);
		return patient;
	}
	//Todo: Add prescription record one by one --- Bundle multiples
	public static Binary populateBinaryResource(String documentId, FHIRAdditionalDetails fhirAdditionalDetails) {
		Binary binary = new Binary();
		binary.setId(documentId);
		binary.getMeta().addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Binary");
		binary.setContentType(fhirAdditionalDetails.getContentType());
		binary.setDataElement(new Base64BinaryType(
				fhirAdditionalDetails.getBase64Content()));
		return binary;
	}

	public static DocumentReference populateDocumentReferenceResource(String documentId,FHIRAdditionalDetails fhirAdditionalDetails) {
		DocumentReference documentReference = new DocumentReference();
		documentReference.setId(documentId);
		documentReference.getMeta().addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentReference");
		documentReference.setStatus(DocumentReferenceStatus.CURRENT);
		documentReference.setDocStatus(ReferredDocumentStatus.FINAL);
		//Todo: Set All these values dynamically
		documentReference.getContent()
				.add(new DocumentReferenceContentComponent(new Attachment().setContentType(fhirAdditionalDetails.getContentType())
						.setLanguage("en-IN").setTitle(fhirAdditionalDetails.getFileName())
						.setCreationElement(DateTimeType.now())
						// TODO OPTIMIZATION.
						.setDataElement(new Base64BinaryType(fhirAdditionalDetails.getBase64Content().split(",")[1]))));
		return documentReference;
	}

	public static Encounter populateEncounterResource(String id) {
		Encounter encounter = new Encounter();
		encounter.setId(id);
		encounter.setStatus(EncounterStatus.FINISHED);
		encounter.getMeta().setLastUpdatedElement(new InstantType(InstantType.now()))
				.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Encounter");
		encounter.getIdentifier().add(new Identifier().setSystem("https://ndhm.in").setValue("S100"));
		encounter.setClass_(
				new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", "ambulatory"));
		return encounter;
	}

	public static Organization populateOrganizationResource(String organizationId, FHIRAdditionalDetails fhirAdditionalDetails) {
		Organization organization = new Organization();
		organization.setId(organizationId);
		organization.getMeta().addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization");
		organization.getIdentifier()
				.add(new Identifier()
						.setType(new CodeableConcept(
								new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "PRN", "Provider number")))
						.setSystem("https://facility.ndhm.gov.in").setValue("4567878"));
		organization.setName(fhirAdditionalDetails.getOrganizationName());
		return organization;
	}

}
