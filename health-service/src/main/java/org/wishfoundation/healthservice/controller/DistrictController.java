package org.wishfoundation.healthservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.healthservice.request.DistrictRequest;
import org.wishfoundation.healthservice.response.DistrictResponse;
import org.wishfoundation.healthservice.service.DistrictServiceImpl;

import java.util.List;


/**
 * Controller for handling district related operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/district")
public class DistrictController {

    /**
     * Service for handling district related operations.
     */
    private final DistrictServiceImpl districtService;

    /**
     * Get all districts for a given state code.
     *
     * @param stateCode The state code for which to retrieve districts.
     * @return A list of {@link DistrictResponse} objects representing the districts.
     */
    @GetMapping("/all/state/{stateCode}")
    public List<DistrictResponse> getDistrict(@PathVariable("stateCode") String stateCode){
       return  districtService.getDistricts(stateCode);
    }

    /**
     * Get district code based on district name and state code.
     *
     * @param districtRequest The request object containing district name and state code.
     * @return A {@link DistrictResponse} object representing the district.
     */
    @PostMapping("/get-code")
    public DistrictResponse getDistrict(@RequestBody DistrictRequest districtRequest){
        return  districtService.getDistrictsCode(districtRequest);
    }

    /**
     * Save a new district.
     *
     * @param districtRequest The request object containing district details.
     * @return A {@link DistrictResponse} object representing the saved district.
     */
    //Todo comment this api for save
    @GetMapping("/internal/save")
    public DistrictResponse saveDistrict(@Valid @RequestBody DistrictRequest districtRequest){
        return  districtService.saveDistrict(districtRequest);
    }
}
