package org.wishfoundation.userservice.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VitalsRecordRequest {
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
    private String age;
    private String bmi;
    private String consultationId;
}
