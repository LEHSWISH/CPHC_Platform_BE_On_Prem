package org.wishfoundation.healthservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
public class DistrictRequest {
    @NotBlank(message = "error.validation.invalidDistrictCode")
    String districtCode;
    @NotBlank(message = "error.validation.invalidDistrictName")
    String districtName;
    @NotBlank(message = "error.validation.invalidStateCode")
    String stateCode;
}
