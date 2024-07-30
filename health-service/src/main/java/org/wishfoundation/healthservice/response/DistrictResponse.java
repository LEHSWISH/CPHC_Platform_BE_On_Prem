package org.wishfoundation.healthservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DistrictResponse {
    private String districtCode;
    private String districtName;
    private String stateCode;
}
