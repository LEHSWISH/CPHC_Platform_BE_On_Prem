package org.wishfoundation.userservice.request.health;

import lombok.Data;

@Data
public class HealthModel {
    String districtCode;
    String districtName;
    String stateCode;
    String stateName;
    String countryCode;
    String countryName;
}
