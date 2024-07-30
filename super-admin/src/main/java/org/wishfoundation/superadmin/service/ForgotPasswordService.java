package org.wishfoundation.superadmin.service;


import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.wishfoundation.superadmin.request.PasswordResetRequest;
import org.wishfoundation.superadmin.request.RegisterUserRequest;
import org.wishfoundation.superadmin.request.ValidateEmailRequest;
import org.wishfoundation.superadmin.response.PasswordResetResponse;

/**
 * This interface represents the service for handling forgot password operations.
 * It provides methods for validating email and resetting password.
 */
@Service
public interface ForgotPasswordService {

    /**
     * Validates the email of the user.
     *
     * @param validatePhoneNumberUserName The request object containing the email to be validated.
     * @return ResponseEntity<Void> A response entity indicating whether the email is valid or not.
     *         Returns HTTP 200 OK if the email is valid, otherwise returns HTTP 400 Bad Request.
     */
    ResponseEntity<Void> validateEmail(ValidateEmailRequest validatePhoneNumberUserName);

    /**
     * Resets the password of the user.
     *
     * @param passwordResetRequest The request object containing the new password and the email.
     * @return ResponseEntity<PasswordResetResponse> A response entity containing the result of the password reset operation.
     *         Returns HTTP 200 OK with the password reset response if successful, otherwise returns HTTP 400 Bad Request.
     */
    ResponseEntity<PasswordResetResponse> resetPassword(PasswordResetRequest passwordResetRequest);

}
