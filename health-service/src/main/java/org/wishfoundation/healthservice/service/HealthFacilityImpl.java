package org.wishfoundation.healthservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.healthservice.entity.DistrictRepository;
import org.wishfoundation.healthservice.entity.HealthFacility;
import org.wishfoundation.healthservice.entity.HealthFacilityRepository;
import org.wishfoundation.healthservice.entity.StateRepository;
import org.wishfoundation.healthservice.enums.ErrorCode;
import org.wishfoundation.healthservice.exception.WishFoundationException;
import org.wishfoundation.healthservice.request.RedisServiceRequest;
import org.wishfoundation.healthservice.response.HealthFacilityResponse;
import org.wishfoundation.healthservice.utils.Helper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class implements the HealthFacilityService interface and provides methods for retrieving and saving health facility data.
 * It uses Redis for caching to improve performance.
 */
@RequiredArgsConstructor
@Service
public class HealthFacilityImpl implements HealthFacilityService {

    /**
     * The repository for accessing health facility data.
     */
    private final HealthFacilityRepository healthFacilityRepository;

    /**
     * The repository for accessing state data.
     */
    private final StateRepository stateRepository;

    /**
     * The repository for accessing district data.
     */
    private final DistrictRepository districtRepository;

    /**
     * The Redis service for caching.
     */
    private final RedisService redisService;

    /**
     * Retrieves health facility data based on the provided state and district codes.
     * If the data is found in Redis, it is returned from there. Otherwise, it is fetched from the database,
     * cached in Redis, and then returned.
     *
     * @param stateCode The code of the state.
     * @param districtCode The code of the district.
     * @return A list of HealthFacilityResponse objects representing the health facility data.
     * @throws WishFoundationException If the state and district codes are not provided or if they are invalid.
     */
    public List<HealthFacilityResponse> getHealthDetails(String stateCode, String districtCode) {
        // Check if stateCode and districtCode are provided
        if (ObjectUtils.isEmpty(stateCode) && ObjectUtils.isEmpty(districtCode)) {
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(), "State and District " + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // Check if stateCode and districtCode are valid
        if (!stateRepository.existsByStateCode(stateCode) && !districtRepository.existsByDistrictCode(districtCode)) {
            throw new WishFoundationException(ErrorCode.INVALID_STATE_AND_DISTRICT.getCode(), ErrorCode.INVALID_STATE_AND_DISTRICT.getMessage(),HttpStatus.NOT_FOUND);
        }

        try {
            // Create RedisServiceRequest object
            RedisServiceRequest redisServiceRequest = RedisServiceRequest.builder().key("HEALTH_FACILITY_STATE_" + stateCode+"_DISTRICT_"+districtCode).build();

            // Check if data is available in Redis
            if (!ObjectUtils.isEmpty(redisService.getRedisValue(redisServiceRequest))){
                // Return data from Redis
                return Helper.MAPPER.readValue(redisService.getRedisValue(redisServiceRequest), new TypeReference<List<HealthFacilityResponse>>() {});
            }else {
                // Fetch data from database
                List<HealthFacility> healthFacilityList = healthFacilityRepository.findByStateCodeAndDistrictCode(stateCode, districtCode);

                // Check if data is found in database
                if (ObjectUtils.isEmpty(healthFacilityList)) {
                    return Collections.emptyList();
                } else {
                    // Convert HealthFacility objects to HealthFacilityResponse objects
                    List<HealthFacilityResponse> healthFacilityResponses =  healthFacilityList.stream().map(district -> Helper.MAPPER.convertValue(district, HealthFacilityResponse.class)).collect(Collectors.toList());

                    // Cache data in Redis
                    redisService.setRedisValue(redisServiceRequest.toBuilder().value(Helper.MAPPER.writeValueAsString(healthFacilityResponses)).timeout(86400).unit(TimeUnit.SECONDS).build());

                    return healthFacilityResponses;
                }
            }
        } catch (Exception e) {
            // Handle exceptions
            throw new WishFoundationException(e.getMessage());
        }
    }

    /**
     * Saves a health facility object to the database.
     *
     * @param healthFacility The health facility object to be saved.
     * @return The saved health facility object.
     */
    public HealthFacility saveHealthDetails(HealthFacility healthFacility) {
        return healthFacilityRepository.save(healthFacility);
    }
}
