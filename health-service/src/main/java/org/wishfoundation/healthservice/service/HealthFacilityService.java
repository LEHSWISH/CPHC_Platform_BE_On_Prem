package org.wishfoundation.healthservice.service;

import org.wishfoundation.healthservice.response.HealthFacilityResponse;

import java.util.List;

/**
 * This interface defines the contract for retrieving health facility details.
 *
 * @version 1.0
 * @since 2022-01-01
 */
public interface HealthFacilityService {

    /**
     * Retrieves health facility details based on the given state and district codes.
     *
     * @param stateCode The code of the state for which health facility details are required.
     * @param districtCode The code of the district within the state for which health facility details are required.
     * @return A list of {@link HealthFacilityResponse} objects containing the health facility details.
     */
    List<HealthFacilityResponse> getHealthDetails(String stateCode, String districtCode);
}
