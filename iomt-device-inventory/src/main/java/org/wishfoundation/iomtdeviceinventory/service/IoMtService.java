package org.wishfoundation.iomtdeviceinventory.service;

import org.wishfoundation.iomtdeviceinventory.request.IoMtLoginRequest;
import org.wishfoundation.iomtdeviceinventory.response.IotMtLoginResponse;
import org.wishfoundation.iomtdeviceinventory.response.IotResponse;

/**
 * This interface defines the methods for the IoMT (Internet of Medical Things) service.
 * It provides functionalities for user login and token verification.
 */
public interface IoMtService {

    /**
     * This method handles user login and generates a token for the IoMT service.
     *
     * @param ioMtLoginRequest The request object containing the user credentials.
     * @return An {@link IotMtLoginResponse} object containing the login status and token.
     */
    IotMtLoginResponse iomtLoginToken(IoMtLoginRequest ioMtLoginRequest);

    /**
     * This method verifies the validity of a given token for a specific entity and communication address.
     *
     * @param token The token to be verified.
     * @param entityid The unique identifier of the entity.
     * @param communicationaddr The communication address of the entity.
     * @return An {@link IotResponse} object indicating the verification status.
     */
    IotResponse iomtVerifyToken(String token, String entityid, String communicationaddr);
}