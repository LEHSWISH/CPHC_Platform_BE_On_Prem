package org.wishfoundation.healthservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
public class StateRequest {
    @NotBlank(message = "error.validation.invalidStateCode")
    String stateCode;
    @NotBlank(message = "error.validation.invalidStateName")
    String stateName;
    String countryCode;
    String countryName;

}
