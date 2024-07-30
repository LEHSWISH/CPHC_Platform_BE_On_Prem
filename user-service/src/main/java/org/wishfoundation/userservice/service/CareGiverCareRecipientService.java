package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.enums.RequestStatus;
import org.wishfoundation.userservice.response.CareGiverCareRecipientResponse;
import org.wishfoundation.userservice.response.LoginResponse;

import java.util.List;
import java.util.UUID;

/**
 * This interface defines the methods for managing caregiver care recipient relationships.
 */
public interface CareGiverCareRecipientService {

    /**
     * Sends a request from a care recipient to a caregiver.
     *
     * @param userName The username of the care recipient.
     * @return ResponseEntity with HTTP status 200 (OK) if the request is sent successfully,
     * or an appropriate error response if the request fails.
     */
    ResponseEntity<Void> sendRequest(String userName);

    /**
     * Retrieves a list of caregiver requests made by a care recipient.
     *
     * @param userId The unique identifier of the care recipient.
     * @return ResponseEntity containing a CareGiverCareRecipientResponse object with a list of caregiver requests,
     * or an appropriate error response if the retrieval fails.
     */
    ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRequests(UUID userId);

    /**
     * Retrieves the details of a caregiver-care recipient relationship for a specific care recipient.
     *
     * @param userId The unique identifier of the care recipient.
     * @return ResponseEntity containing a CareGiverCareRecipientResponse object with the relationship details,
     * or an appropriate error response if the retrieval fails.
     */
    ResponseEntity<CareGiverCareRecipientResponse> viewCareGiverRecipient(UUID userId);

    /**
     * Responds to a caregiver request from a care recipient.
     *
     * @param requestStatus The status of the response (ACCEPTED, REJECTED).
     * @param userName The username of the care recipient.
     * @return ResponseEntity with HTTP status 200 (OK) if the response is processed successfully,
     * or an appropriate error response if the response fails.
     */
    ResponseEntity<Void> respondRequest(RequestStatus requestStatus, String userName);

    /**
     * Removes a care recipient from a caregiver's list of recipients.
     *
     * @param userName The username of the care recipient.
     * @return ResponseEntity with HTTP status 200 (OK) if the removal is successful,
     * or an appropriate error response if the removal fails.
     */
    ResponseEntity<Void> removeRecipient(String userName);

    /**
     * Retrieves a JWT token for a user based on their username and session ID.
     *
     * @param userName The username of the user.
     * @param sessionId The session ID of the user.
     * @return ResponseEntity containing a LoginResponse object with the JWT token,
     * or an appropriate error response if the token retrieval fails.
     */
    ResponseEntity<LoginResponse> getToken(String userName, String sessionId);

    /**
     * Removes a caregiver from a care recipient's list of caregivers.
     *
     * @param userName The unique identifier of the caregiver.
     * @return ResponseEntity with HTTP status 200 (OK) if the removal is successful,
     * or an appropriate error response if the removal fails.
     */
    ResponseEntity<Void> removeCareGiver(UUID userName);

    /**
     * Retrieves a list of caregiver-care recipient relationships associated with a specific phone number.
     *
     * @param phoneNumber The phone number to search for.
     * @return A list of CareGiverCareRecipientResponse objects with the relationship details.
     */
    List<CareGiverCareRecipientResponse> phoneNumberLinkedUsers(String phoneNumber);
}
