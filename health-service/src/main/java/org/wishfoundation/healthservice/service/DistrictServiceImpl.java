package org.wishfoundation.healthservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.healthservice.entity.District;
import org.wishfoundation.healthservice.entity.DistrictRepository;
import org.wishfoundation.healthservice.entity.State;
import org.wishfoundation.healthservice.entity.StateRepository;
import org.wishfoundation.healthservice.enums.ErrorCode;
import org.wishfoundation.healthservice.exception.WishFoundationException;
import org.wishfoundation.healthservice.request.DistrictRequest;
import org.wishfoundation.healthservice.request.RedisServiceRequest;
import org.wishfoundation.healthservice.response.DistrictResponse;
import org.wishfoundation.healthservice.response.StateResponse;
import org.wishfoundation.healthservice.utils.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is responsible for implementing the DistrictService interface.
 * It provides methods for retrieving and saving district data.
 */
@AllArgsConstructor
@Service
public class DistrictServiceImpl implements DistrictService {

    /**
     * The DistrictRepository interface for database operations on District entities.
     */
    private final DistrictRepository districtRepository;

    /**
     * The StateRepository interface for database operations on State entities.
     */
    private final StateRepository stateRepository;

    /**
     * The RedisService interface for interacting with Redis cache.
     */
    private final RedisService redisService;

    /**
     * Retrieves a list of districts for a given state code.
     *
     * @param stateCode The state code for which to retrieve districts.
     * @return A list of DistrictResponse objects representing the districts.
     * @throws WishFoundationException If the state code is not provided or invalid.
     */
    @Override
    public List<DistrictResponse> getDistricts(String stateCode) {
        // Check if state code is provided
        if (ObjectUtils.isEmpty(stateCode)) {
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(), "State " + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);
        }
        // Check if state code exists in the database
        if (!stateRepository.existsByStateCode(stateCode)) {
            throw new WishFoundationException(ErrorCode.INVALID_STATE_CODE.getCode(), ErrorCode.INVALID_STATE_CODE.getMessage(),HttpStatus.NOT_FOUND);
        }
        try {
            // Prepare Redis request
            RedisServiceRequest redisServiceRequest = RedisServiceRequest.builder().key("DISTRICT_LIST_STATE_" + stateCode).build();
            // Check if data is available in Redis cache
            if (!ObjectUtils.isEmpty(redisService.getRedisValue(redisServiceRequest))) {
                // Return data from Redis cache
                return Helper.MAPPER.readValue(redisService.getRedisValue(redisServiceRequest), new TypeReference<List<DistrictResponse>>() {
                });
            } else {
                // Fetch data from database
                List<District> districtList = districtRepository.findByStateCode(stateCode);
                if (ObjectUtils.isEmpty(districtList)) {
                    return Collections.emptyList();
                } else {
                    // Convert District entities to DistrictResponse objects
                    List<DistrictResponse> districtResponses = districtList.stream().map(district -> Helper.MAPPER.convertValue(district, DistrictResponse.class)).collect(Collectors.toList());
                    // Store data in Redis cache
                    redisService.setRedisValue(redisServiceRequest.toBuilder().value(Helper.MAPPER.writeValueAsString(districtResponses)).timeout(86400).unit(TimeUnit.SECONDS).build());
                    return districtResponses;
                }
            }
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }
    }

    /**
     * Saves a new district to the database.
     *
     * @param districtRequest The DistrictRequest object containing the district data.
     * @return The DistrictResponse object representing the saved district.
     * @throws WishFoundationException If the state code is invalid.
     */
    @Override
    public DistrictResponse saveDistrict(DistrictRequest districtRequest) {
        // Check if state code exists in the database
        if (!stateRepository.existsByStateCode(districtRequest.getStateCode())) {
            throw new WishFoundationException(ErrorCode.INVALID_STATE_CODE.getCode(), ErrorCode.INVALID_STATE_CODE.getMessage(),HttpStatus.NOT_FOUND);
        }
        try {
            // Convert DistrictRequest object to District entity
            District districtDb = Helper.MAPPER.convertValue(districtRequest, District.class);
            // Save district to the database
            return Helper.MAPPER.convertValue(districtRepository.save(districtDb), DistrictResponse.class);
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }

    }

    /**
     * Retrieves the district code and name for a given state code and district name.
     *
     * @param districtRequest The DistrictRequest object containing the state code and district name.
     * @return The DistrictResponse object representing the district.
     * @throws WishFoundationException If the state code or district name is invalid.
     */
    public DistrictResponse getDistrictsCode(DistrictRequest districtRequest) {
        try{
            // Fetch district by state code and district name
            Optional<District> byStateCodeAndDistrictName = districtRepository.findByStateCodeAndDistrictName(districtRequest.getStateCode(), districtRequest.getDistrictName());
            if(byStateCodeAndDistrictName.isPresent()) {
                District district = byStateCodeAndDistrictName.get();
                System.out.println("district : "+Helper.MAPPER.writeValueAsString(district));
                // Return DistrictResponse object
                return DistrictResponse.builder().districtCode(district.getDistrictCode()).districtName(district.getDistrictName()).stateCode(district.getStateCode()).build();
            }else
                throw new WishFoundationException("400", "Invaild StateCode or District Name");
        }catch (Exception e){
            e.printStackTrace();
            throw new WishFoundationException("400", "Invaild StateCode or District Name");
        }
    }
}
