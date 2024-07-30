package org.wishfoundation.healthservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class HealthFacilityResponse {
    private String facilityId;
    private String facilityName;
    private String facilityType;
    private String ownership;
    private String address;
}
