package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.response.LoginResponse;
import org.wishfoundation.userservice.response.PhoneNumberLinkedResponse;

/**
 * This interface defines the contract for YatriPulseUserLoginSignUpService.
 * It provides methods for user login, registration, validation, phone number linking, and checking user existence.
 */
public interface YatriPulseUserLoginSignUpService {

    /**
     * Logs in a user using the provided YatriPulseUserRequest.
     *
     * @param yatriPulseUserRequest The request containing user credentials.
     * @return ResponseEntity<LoginResponse> containing the login response.
     */
    ResponseEntity<LoginResponse> login(YatriPulseUserRequest yatriPulseUserRequest);

    /**
     * Registers a new Yatri using the provided YatriPulseUserRequest.
     *
     * @param yatriPulseUserRequest The request containing user details.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    ResponseEntity<Void> registerYatri(YatriPulseUserRequest yatriPulseUserRequest);

    /**
     * Validates a user using the provided username.
     *
     * @param userName The username to validate.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    ResponseEntity<Void> validateUser(String userName);

    /**
     * Checks if a phone number is linked to a user.
     *
     * @param phoneNumber The phone number to check.
     * @param fetchUserFlag A flag indicating whether to fetch user details.
     * @return PhoneNumberLinkedResponse containing the result.
     */
    PhoneNumberLinkedResponse phoneNumberLinked(String phoneNumber, boolean fetchUserFlag);

    /**
     * Checks if a user with the provided username exists.
     *
     * @param userName The username to check.
     * @return ResponseEntity<Void> indicating success or failure.
     */
    ResponseEntity<Void> checkUserExistence(String userName);
}
