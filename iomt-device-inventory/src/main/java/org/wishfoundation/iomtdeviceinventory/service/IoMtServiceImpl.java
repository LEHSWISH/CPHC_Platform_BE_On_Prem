package org.wishfoundation.iomtdeviceinventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wishfoundation.iomtdeviceinventory.enums.ErrorCode;
import org.wishfoundation.iomtdeviceinventory.exception.WishFoundationException;
import org.wishfoundation.iomtdeviceinventory.request.IoMtLoginRequest;
import org.wishfoundation.iomtdeviceinventory.response.IotMtLoginResponse;
import org.wishfoundation.iomtdeviceinventory.response.IotResponse;
import org.wishfoundation.iomtdeviceinventory.security.JWTService;
import org.wishfoundation.iomtdeviceinventory.utils.EnvironmentConfig;

/**
 * This class implements the IoMtService interface and provides methods for handling
 * IoT (Internet of Things) device inventory management.
 */
@RequiredArgsConstructor
@Service
public class IoMtServiceImpl implements IoMtService {

    /**
     * A builder for creating instances of {@link WebClient}.
     */
    private final WebClient.Builder webClient;

    /**
     * A service for handling JWT (JSON Web Tokens).
     */
    private final JWTService jwtService;

    /**
     * A constant representing an internal request header.
     */
    public static final String INTERNAL_REQUEST = "x-internal-request";

    /**
     * This method handles the login process for IoT devices. It validates the user's
     * service token, sends a request to the YATRI service to validate the token, and
     * generates a JWT token for the user.
     *
     * @param ioMtLoginRequest The request object containing the user's service token.
     * @return An {@link IotMtLoginResponse} object containing the generated JWT token.
     * @throws WishFoundationException If the user's service token is invalid or the login fails.
     */
    @Override
    public IotMtLoginResponse iomtLoginToken(IoMtLoginRequest ioMtLoginRequest) {
        if (ObjectUtils.isEmpty(ioMtLoginRequest.getUserServiceToken()))
            throw new WishFoundationException(ErrorCode.INVALID_VALUE.getCode(), "token" + ErrorCode.INVALID_VALUE.getMessage(), HttpStatus.BAD_REQUEST);

        WebClient client = webClient.baseUrl(EnvironmentConfig.YATRI_SERVICE_HOST).build();
        IotResponse iotResponse = client.post()
                .uri("/api/iomt/validate-token")
                .header("Content-Type", "application/json")
                .bodyValue(ioMtLoginRequest)
                .retrieve()
                .bodyToMono(IotResponse.class)
                .block();

        if (iotResponse.getCode() == 200) {
            String userName = jwtService.getUsernameFromJWT(ioMtLoginRequest.getUserServiceToken());
            String phoneNumber = jwtService.getPhoneNumberFromJWT(ioMtLoginRequest.getUserServiceToken());
            String token = jwtService.generateToken(userName, phoneNumber);
            return IotMtLoginResponse.builder().token(token).phoneNumber(phoneNumber).userName(userName).build();
        }

        throw new WishFoundationException(ErrorCode.LOGIN_FAILED.getCode(), ErrorCode.LOGIN_FAILED.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * This method verifies the validity of a JWT token.
     *
     * @param token The JWT token to be verified.
     * @param entityid The entity ID associated with the token.
     * @param communicationaddr The communication address associated with the token.
     * @return An {@link IotResponse} object with a success message if the token is valid.
     * @throws WishFoundationException If the token is invalid.
     */
    @Override
    public IotResponse iomtVerifyToken(String token, String entityid, String communicationaddr) {
        if (ObjectUtils.isEmpty(token))
            throw new WishFoundationException(ErrorCode.INVALID_VALUE.getCode(), ErrorCode.INVALID_VALUE.getMessage(), HttpStatus.BAD_REQUEST);

        if (jwtService.validateToken(token))
            return IotResponse.builder().code(200).message("Success").build();

        throw new WishFoundationException(ErrorCode.INVALID_TOKEN.getCode(), ErrorCode.INVALID_TOKEN.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
