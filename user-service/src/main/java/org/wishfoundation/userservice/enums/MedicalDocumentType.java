package org.wishfoundation.userservice.enums;

public enum MedicalDocumentType {
    OPConsultation("OP Consultation"),
    DiagnosticReport("Diagnostic Report"),
    DischargeSummary("Discharge Summary"),
    Prescription("Prescription"),
    ImmunizationRecord("Immunization Record"),
    HealthDocumentRecord("Record artifact"),
    WellnessRecord("Wellness Record");


    private final String display;

    MedicalDocumentType(String display) {
        this.display = display;
    }
}
