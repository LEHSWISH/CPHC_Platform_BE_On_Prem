package org.wishfoundation.userservice.service;

import org.wishfoundation.userservice.request.IotRequest;
import org.wishfoundation.userservice.response.IotResponse;

/**
 * This interface represents the IOT (Internet of Things) service.
 * It provides methods for interacting with IOT devices and users.
 */
public interface IomtService {

    /**
     * Validates a user based on the provided IOT request.
     *
     * @param iotRequest The request containing user details and IOT device information.
     * @return An {@link IotResponse} object containing the validation result and any relevant data.
     */
    IotResponse validateUser(IotRequest iotRequest);
}
