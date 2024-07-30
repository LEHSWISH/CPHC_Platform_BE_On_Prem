package org.wishfoundation.abhaservice.enums;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
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

    public static List<MedicalDocumentType> getAll() {
        return Arrays.asList(OPConsultation, DiagnosticReport, DischargeSummary, Prescription, ImmunizationRecord, HealthDocumentRecord,WellnessRecord);
    }
}
