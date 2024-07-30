package org.wishfoundation.healthservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.healthservice.entity.State;
import org.wishfoundation.healthservice.request.StateRequest;
import org.wishfoundation.healthservice.response.StateResponse;
import org.wishfoundation.healthservice.service.StateServiceImpl;

import java.util.List;

/**
 * This is the controller for handling state related operations.
 * It provides endpoints for retrieving all states, getting state by name, and saving a new state.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/state")
public class StateController {

    /**
     * Service for handling state related operations.
     */
	private final StateServiceImpl stateService;

    /**
     * Endpoint for retrieving all states.
     *
     * @return a list of {@link StateResponse} objects representing all states.
     */
	@GetMapping("/all")
	public List<StateResponse> getState() {
		return stateService.getState();
	}

    /**
     * Endpoint for getting state by name.
     *
     * @param stateRequest a {@link StateRequest} object containing the name of the state.
     * @return a {@link State} object representing the state with the given name.
     */
	@PostMapping("/get-code")
	public State getStateId(@RequestBody StateRequest stateRequest) {
		return stateService.getStateByName(stateRequest);
	}

    /**
     * Endpoint for saving a new state.
     * This endpoint is intended for internal use only.
     *
     * @param stateRequest a {@link StateRequest} object containing the details of the new state.
     * @return a {@link StateResponse} object representing the saved state.
     */
	@GetMapping("/internal/save")
	public StateResponse saveState(@Valid @RequestBody StateRequest stateRequest){
		return stateService.saveState(stateRequest);
	}
}
