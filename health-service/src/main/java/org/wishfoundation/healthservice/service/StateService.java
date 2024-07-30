package org.wishfoundation.healthservice.service;

import org.wishfoundation.healthservice.request.StateRequest;
import org.wishfoundation.healthservice.response.StateResponse;

import java.util.List;

/**
 * This interface defines the contract for operations related to State.
 * It provides methods to retrieve a list of states and save a new state.
 */
public interface StateService {

    /**
     * Retrieves a list of all states.
     *
     * @return a list of {@link StateResponse} objects representing the states.
     */
    List<StateResponse> getState();

    /**
     * Saves a new state.
     *
     * @param stateRequest a {@link StateRequest} object containing the details of the state to be saved.
     * @return a {@link StateResponse} object representing the saved state.
     */
    StateResponse saveState(StateRequest stateRequest);
}
