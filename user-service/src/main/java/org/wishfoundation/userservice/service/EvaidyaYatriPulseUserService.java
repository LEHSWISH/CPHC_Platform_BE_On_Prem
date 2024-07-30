package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.response.EvaidyaUserResponse;
import org.wishfoundation.userservice.response.UserDetailsResponse;

/**
 * This interface defines the contract for the Evaidya Yatri Pulse User Service.
 * It provides methods for retrieving user details, signing up, and updating user information.
 */
public interface EvaidyaYatriPulseUserService {

    /**
     * Retrieves the user details based on the provided username.
     *
     * @param username The unique identifier of the user.
     * @return The {@link UserDetailsResponse} object containing the user details.
     */
    UserDetailsResponse getYatriDetails(String username);

    /**
     * Registers a new user using the provided basic information of the user.
     *
     * @param yatriPulseUserRequest The {@link YatriPulseUserRequest} object containing the user information.
     * @return The {@link ResponseEntity} containing the {@link EvaidyaUserResponse} object.
     */
    ResponseEntity<EvaidyaUserResponse> signUp(YatriPulseUserRequest yatriPulseUserRequest);

    /**
     * Updates the user information using the provided Yatri Pulse User Request.
     *
     * @param yatriPulseUserRequest The {@link YatriPulseUserRequest} object containing the updated user information.
     * @return The {@link ResponseEntity} with a {@link Void} object.
     */
    ResponseEntity<Void> updateUser(YatriPulseUserRequest yatriPulseUserRequest);
}
