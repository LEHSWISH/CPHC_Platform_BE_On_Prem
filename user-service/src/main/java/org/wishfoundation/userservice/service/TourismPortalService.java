package org.wishfoundation.userservice.service;

import org.wishfoundation.userservice.response.TourismUserDetails;

/**
 * This interface represents the contract for interacting with the Tourism Portal Service.
 * It provides methods to retrieve user details based on their ID and consent status.
 */
public interface TourismPortalService {
    /**
     * Retrieves the user details from the Tourism Portal Service based on the provided ID and consent status.
     *
     * @param id       The unique identifier of the user.
     * @param consent  A boolean indicating whether the user has given consent for data retrieval.
     * @return         A {@link TourismUserDetails} object containing the user's details.
     *                 If the user is not found or consent is not given, returns null.
     */
    TourismUserDetails getUserByIDTP(String id, boolean consent);
}
