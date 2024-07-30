package org.wishfoundation.healthservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.healthservice.entity.State;
import org.wishfoundation.healthservice.entity.StateRepository;
import org.wishfoundation.healthservice.exception.WishFoundationException;
import org.wishfoundation.healthservice.request.RedisServiceRequest;
import org.wishfoundation.healthservice.request.StateRequest;
import org.wishfoundation.healthservice.response.StateResponse;
import org.wishfoundation.healthservice.utils.Helper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class implements the StateService interface and provides methods for managing states.
 */
@RequiredArgsConstructor
@Service
public class StateServiceImpl implements StateService {

    /**
     * The StateRepository interface for database operations.
     */
    private final StateRepository stateRepository;

    /**
     * The RedisService for caching state data.
     */
    private final RedisService redisService;

    /**
     * Retrieves a list of all states from the database or cache.
     *
     * @return a list of StateResponse objects representing the states.
     * @throws WishFoundationException if an error occurs during retrieval.
     */
    public List<StateResponse> getState() {
        try {
            RedisServiceRequest redisServiceRequest = RedisServiceRequest.builder().key("STATE_LIST").build();
            if (!ObjectUtils.isEmpty(redisService.getRedisValue(redisServiceRequest))) {
                return Helper.MAPPER.readValue(redisService.getRedisValue(redisServiceRequest), new TypeReference<List<StateResponse>>() {
                });
            } else {
                List<State> stateList = stateRepository.findAll();
                if (ObjectUtils.isEmpty(stateList)) {
                    return Collections.emptyList();
                } else {
                    List<StateResponse> stateResponses = stateList.stream().map(state -> Helper.MAPPER.convertValue(state, StateResponse.class)).collect(Collectors.toList());
                    redisService.setRedisValue(redisServiceRequest.toBuilder().value(Helper.MAPPER.writeValueAsString(stateResponses)).timeout(86400).unit(TimeUnit.SECONDS).build());
                    return stateResponses;
                }
            }
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }
    }

    /**
     * Saves a new state to the database.
     *
     * @param stateRequest the StateRequest object containing the state data.
     * @return the StateResponse object representing the saved state.
     * @throws WishFoundationException if an error occurs during saving.
     */
    @Override
    public StateResponse saveState(StateRequest stateRequest) {
        try {
            State stateDb = Helper.MAPPER.convertValue(stateRequest, State.class);
            return Helper.MAPPER.convertValue(stateRepository.save(stateDb), StateResponse.class);
        } catch (Exception e) {
            throw new WishFoundationException(e.getMessage());
        }
    }

    /**
     * Retrieves a state by its name from the database.
     *
     * @param stateRequest the StateRequest object containing the state name.
     * @return the State object representing the state.
     * @throws WishFoundationException if the state is not found or an error occurs during retrieval.
     */
    public State getStateByName(StateRequest stateRequest) {
        try {
            State s = null;
            Optional<State> byStateName = stateRepository.findByStateName(stateRequest.getStateName());
            if (byStateName.isPresent()) {
                s = byStateName.get();
                System.out.println(Helper.MAPPER.writeValueAsString(s));
                return s;
            } else
                throw new WishFoundationException("400", "Invaild State");
        } catch (Exception e) {
            e.printStackTrace();
            throw new WishFoundationException("400", "Invaild State");
        }
    }
}
