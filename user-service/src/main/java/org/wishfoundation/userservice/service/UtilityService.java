package org.wishfoundation.userservice.service;

import org.wishfoundation.userservice.response.PinCodeResponse;

/**
 * This interface represents a utility service that provides various utility functionalities.
 *
 */
public interface UtilityService {

    /**
     * Retrieves information related to a specific pin code.
     *
     * @param pinCode The pin code for which information is required.
     * @return A {@link PinCodeResponse} object containing the information related to the pin code.
     *         If no information is found, the response will be null.
     */
    PinCodeResponse getInfoByPinCode(String pinCode);
}
