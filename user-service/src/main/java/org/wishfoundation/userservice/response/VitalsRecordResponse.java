package org.wishfoundation.userservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.userservice.enums.HealthStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VitalsRecordResponse {
    private String systolicBp;
    private String diastolicBp;
    private String meanBp;
    private String heartRate;
    private String spo2;
    private String temperature;
    private String temperatureUnits;
    private String temperatureSource;
    private String ecg;
    private String height;
    private String heightUnits;
    private String weight;
    private String weightUnits;
    private String bloodSugar;
    private String location;
    private String bmi;
    private String age;
    private HealthStatus status;
    private String fullName;
    private String consultationId;

    public VitalsRecordResponse(String systolicBp, String diastolicBp, String meanBp, String heartRate, String spo2, String temperature, String temperatureUnits, String temperatureSource, String ecg, String height, String heightUnits, String weight, String weightUnits, String bloodSugar, String location, String bmi, String age, String consultationId, HealthStatus status, String fullName) {
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.meanBp = meanBp;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.temperature = temperature;
        this.temperatureUnits = temperatureUnits;
        this.temperatureSource = temperatureSource;
        this.ecg = ecg;
        this.height = height;
        this.heightUnits = heightUnits;
        this.weight = weight;
        this.weightUnits = weightUnits;
        this.bloodSugar = bloodSugar;
        this.location = location;
        this.bmi = bmi;
        this.age = age;
        this.consultationId = consultationId;
        this.status = status;
        this.fullName = fullName;
    }

    private String bloodPressureFullValue;
}
