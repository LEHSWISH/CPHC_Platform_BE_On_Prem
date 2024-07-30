package org.wishfoundation.healthservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.healthservice.entity.HealthFacility;
import org.wishfoundation.healthservice.response.HealthFacilityResponse;
import org.wishfoundation.healthservice.service.HealthFacilityImpl;

import java.util.List;

/**
 * Controller for handling health facility related operations.
 *
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/healthFacility")
@RequiredArgsConstructor
public class HealthFacilityController {
    /**
     * Private instance of HealthFacilityImpl for dependency injection.
     */
	private final HealthFacilityImpl healthFacilityImpl;

    /**
     * Retrieves health facility details based on the given state and district codes.
     *
     * @param stateCode  The state code for which to retrieve health facility details.
     * @param districtCode  The district code for which to retrieve health facility details.
     * @return A list of HealthFacilityResponse objects containing health facility details.
     */
	@GetMapping("/all/state/{stateCode}/district/{districtCode}")
	public List<HealthFacilityResponse> getHealthDetails(@PathVariable("stateCode") String stateCode,
														 @PathVariable("districtCode") String districtCode) {
		return healthFacilityImpl.getHealthDetails(stateCode, districtCode);
	}

    /**
     * Saves health facility details to the database.
     *
     * @param healthFacility  The HealthFacility object containing the details to be saved.
     * @return The saved HealthFacility object.
     */
	@PostMapping("/internal/save")
	public HealthFacility saveHealthDetails(@Valid @RequestBody HealthFacility healthFacility) {
		return healthFacilityImpl.saveHealthDetails(healthFacility);
	}
}
