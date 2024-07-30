package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.request.ForgetUserName;
import org.wishfoundation.userservice.request.ValidatePhoneNumberUserName;
import org.wishfoundation.userservice.request.YatriPulseUserRequest;
import org.wishfoundation.userservice.response.PasswordResetResponse;

/**
 * This interface defines the contract for the Forget Password Service.
 * It provides methods for validating user's phone number and username, resetting password, and retrieving username.
 */
public interface ForgetUserPasswordService  {

    /**
     * Validates the user's phone number and username.
     *
     * @param validatePhoneNumberUserName The request object containing the phone number and username to validate.
     * @return ResponseEntity<Void> A response entity indicating success or failure of the validation.
     */
    ResponseEntity<Void> validateUserNamePhoneNumber(ValidatePhoneNumberUserName validatePhoneNumberUserName);

    /**
     * Resets the user's password.
     *
     * @param yatriPulseUserRequest The request object containing the user's new password.
     * @return PasswordResetResponse The response object containing the status of the password reset operation.
     */
    PasswordResetResponse resetPassword(YatriPulseUserRequest yatriPulseUserRequest);

    /**
     * Retrieves the username for a given phone number.
     *
     * @param forgetUserName The request object containing the phone number for which the username needs to be retrieved.
     * @return ResponseEntity<Void> A response entity indicating success or failure of the username retrieval.
     */
    ResponseEntity<Void> forgetUsername(ForgetUserName forgetUserName) ;
}
