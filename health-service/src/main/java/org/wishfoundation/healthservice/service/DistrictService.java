package org.wishfoundation.healthservice.service;

import org.wishfoundation.healthservice.request.DistrictRequest;
import org.wishfoundation.healthservice.response.DistrictResponse;

import java.util.List;

/**
 * This interface defines the contract for operations related to Districts.
 * It provides methods to retrieve a list of districts for a given state code,
 * and to save a new district.
 */
public interface DistrictService {

    /**
     * Retrieves a list of districts for a given state code.
     *
     * @param stateCode The unique identifier of the state for which districts are to be retrieved.
     * @return A list of {@link DistrictResponse} objects representing the districts for the given state.
     */
    List<DistrictResponse> getDistricts(String stateCode);

    /**
     * Saves a new district.
     *
     * @param districtRequest The {@link DistrictRequest} object containing the details of the district to be saved.
     * @return The {@link DistrictResponse} object representing the saved district.
     */
    DistrictResponse saveDistrict(DistrictRequest districtRequest);
}
