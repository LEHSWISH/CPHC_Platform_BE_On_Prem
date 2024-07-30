package org.wishfoundation.superadmin.service;

import ch.qos.logback.classic.encoder.JsonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.superadmin.entity.UserAccounts;
import org.wishfoundation.superadmin.entity.repository.UserAccountsRepository;
import org.wishfoundation.superadmin.enums.ErrorCode;
import org.wishfoundation.superadmin.exception.WishFoundationException;
import org.wishfoundation.superadmin.request.OtpRequest;
import org.wishfoundation.superadmin.request.PasswordResetRequest;
import org.wishfoundation.superadmin.request.RegisterUserRequest;
import org.wishfoundation.superadmin.request.ValidateEmailRequest;
import org.wishfoundation.superadmin.response.PasswordResetResponse;
import org.wishfoundation.superadmin.utils.Helper;

import java.util.Optional;


/**
 * This class implements the ForgotPasswordService interface.
 * It provides methods for validating email and resetting password.
 */
@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    /**
     * UserAccountsRepository instance for database operations.
     */
    @Autowired
    private UserAccountsRepository userAccountsRepository;

    /**
     * AuthServiceImpl instance for OTP verification.
     */
    @Autowired
    private AuthServiceImpl authService;

    /**
     * PasswordEncoder instance for password encoding.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
 * Validates the email provided in the request.
 *
 * @param validateEmailRequest The request containing the email to be validated.
 *                            It should not be null.
 * @return ResponseEntity with HTTP status OK if the email is valid.
 *         If the email is not present in the database, it throws a WishFoundationException
 *         with HTTP status NOT_FOUND and USER_IS_NOT_PRESENT error code.
 *         If the email does not match the user's email in the database, it throws a WishFoundationException
 *         with HTTP status BAD_REQUEST and PHONE_NUMBER_IS_NOT_MATCHED error code.
 * @throws WishFoundationException If the email is not present in the database or does not match the user's email.
 */
    @Override
    public ResponseEntity<Void> validateEmail(ValidateEmailRequest validateEmailRequest) {

        // Check if the email is present in the database
        Optional<UserAccounts> userOpt = userAccountsRepository.findUserByEmail(validateEmailRequest.getEmail());
        if(userOpt.isPresent()){
            UserAccounts user = userOpt.get();

            // Check if the email matches the user's email in the database
            if(!user.getEmail().equals(validateEmailRequest.getEmail())){
                throw new WishFoundationException(ErrorCode.PHONE_NUMBER_IS_NOT_MATCHED.getCode(),
                        ErrorCode.PHONE_NUMBER_IS_NOT_MATCHED.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }  else {
            // If the email is not present in the database, throw an exception
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);
        }

        // If the email is valid, return HTTP status OK
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
 * Resets the password for the user with the provided email.
 *
 * @param passwordResetRequest The request containing the email, OTP, and new password.
 *                            It should not be null.
 * @return ResponseEntity with HTTP status OK and a PasswordResetResponse if the password is successfully reset.
 *         If the email, OTP, or password is not provided, it throws a WishFoundationException
 *         with HTTP status BAD_REQUEST and VALUE_SHOULD_BE_PROVIDED error code.
 *         If the email is not present in the database, it throws a WishFoundationException
 *         with HTTP status NOT_FOUND and USER_IS_NOT_PRESENT error code.
 *         If the OTP is invalid, it throws a WishFoundationException
 *         with HTTP status BAD_REQUEST and INVALID_OTP error code.
 * @throws WishFoundationException If the email, OTP, or password is not provided.
 * @throws WishFoundationException If the email is not present in the database.
 * @throws WishFoundationException If the OTP is invalid.
 */
    @Override
    public ResponseEntity<PasswordResetResponse> resetPassword(PasswordResetRequest passwordResetRequest) {
        String email = passwordResetRequest.getEmail();
        if (ObjectUtils.isEmpty(email))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "email" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        UserAccounts userAccounts = userAccountsRepository.findUserByEmail(email).get();

        if (ObjectUtils.isEmpty(userAccounts))
            throw new WishFoundationException(ErrorCode.USER_IS_NOT_PRESENT.getCode(),
                    ErrorCode.USER_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND);

        if (ObjectUtils.isEmpty(passwordResetRequest.getOtp()))
            throw new WishFoundationException(ErrorCode.VALUE_SHOULD_BE_PROVIDED.getCode(),
                    "OTP" + ErrorCode.VALUE_SHOULD_BE_PROVIDED.getMessage(), HttpStatus.BAD_REQUEST);

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmailId(passwordResetRequest.getEmail());
        otpRequest.setOtp(passwordResetRequest.getOtp());
        otpRequest.setTemplateKey(passwordResetRequest.getTemplateKey());

        authService.verifyOTP(otpRequest);

        String password = Helper.decryptPassword(passwordResetRequest.getPassword());
        String[] PassAndTime = password.split("%");
        userAccounts.setPassword(passwordEncoder.encode(PassAndTime[1]));
        userAccountsRepository.save(userAccounts);

        return ResponseEntity.status(HttpStatus.OK).body(PasswordResetResponse.builder().message("You have successfully reset your password.").build());
    }
}
