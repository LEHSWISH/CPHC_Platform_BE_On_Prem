package org.wishfoundation.healthservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class HealthFacilityRequest {

    @NotBlank(message = "error.validation.invalidDistrictCode")
    String districtCode;
    @NotBlank(message = "error.validation.invalidStateCode")
    String stateCode;

    @NotBlank(message = "error.validation.invalidFacilityId")
    String facilityId;

    @NotBlank(message = "error.validation.invalidFacilityName")
    String facilityName;

    @NotBlank(message = "error.validation.invalidFacilityStatus")
    String facilityStatus;

    @NotBlank(message = "error.validation.invalidFacilityType")
    String facilityType;

    @NotBlank(message = "error.validation.invalidFacilityOwnership")
    String ownership;

    @NotBlank(message = "error.validation.invalidFacilityAddress")
    String address;
    boolean abdmEnabled;

}
