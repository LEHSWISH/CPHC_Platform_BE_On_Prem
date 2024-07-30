package org.wishfoundation.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.exception.WishFoundationException;
import org.wishfoundation.userservice.request.IotRequest;
import org.wishfoundation.userservice.response.IotResponse;
import org.wishfoundation.userservice.security.JWTService;

import java.util.Optional;

/**
 * This class implements the IomtService interface. It provides methods for validating user tokens and retrieving user information.
 */
@Service
public class IomtServiceImpl implements IomtService {

    /**
     * The JWTService instance for token validation and username extraction.
     */
    @Autowired
    private JWTService jwtService;

    /**
     * The YatriPulseUsersRepository instance for querying user data.
     */
    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepository;

    /**
     * Validates the user token and retrieves user information.
     *
     * @param iotRequest The IotRequest object containing the user token.
     * @return An IotResponse object indicating success or failure.
     * @throws WishFoundationException If the token is invalid or the user is not present.
     */
    @Override
    public IotResponse validateUser(IotRequest iotRequest) {
        boolean isValidToken = jwtService.validateToken(iotRequest.getUserServiceToken());
        if (isValidToken) {
            String userName = jwtService.getUsernameFromJWT(iotRequest.getUserServiceToken());
            Optional<YatriPulseUsers> userOpt = yatriPulseUsersRepository.findUserByUserName(userName);
            if (userOpt.isPresent()) {
                return IotResponse.builder().code(200).message("success").build();
            } else {
                throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(), ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        throw new WishFoundationException(ErrorCode.INVALID_TOKEN.getCode(), ErrorCode.INVALID_TOKEN.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
