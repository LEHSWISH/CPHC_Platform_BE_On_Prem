package org.wishfoundation.superadmin.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.superadmin.request.*;
import org.wishfoundation.superadmin.response.LoginResponse;
import org.wishfoundation.superadmin.response.OtpResponse;
import org.wishfoundation.superadmin.response.PasswordResetResponse;
import org.wishfoundation.superadmin.service.AuthServiceImpl;
import org.wishfoundation.superadmin.service.ForgotPasswordService;

/**
 * Controller for handling authentication related requests.
 *
 */
@RestController
@RequestMapping("api/v1/auth/")
public class AuthController {

    /**
     * Service for handling authentication related operations.
     */
    @Autowired
    AuthServiceImpl authService;

    /**
     * Service for handling forgot password related operations.
     */
    @Autowired
    ForgotPasswordService forgotPasswordService;

    /**
     * Handles user login request.
     *
     * @param loginRequest The request containing user credentials.
     * @param sessionId The session id of the user.
     * @return ResponseEntity containing the login response.
     */
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, @RequestHeader("x-session-id") String sessionId) {
        return authService.login(loginRequest, sessionId);
    }

    /**
     * Handles user registration request.
     *
     * @param registerUserRequest The request containing user registration details.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@RequestBody RegisterUserRequest registerUserRequest) {
        return authService.register(registerUserRequest);
    }

    /**
     * Validates the email provided in the request.
     *
     * @param validateEmailRequest The request containing the email to be validated.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("validate-email")
    public ResponseEntity<Void> validateEmail(@RequestBody @Valid ValidateEmailRequest validateEmailRequest){
        return forgotPasswordService.validateEmail(validateEmailRequest);
    }

    /**
     * Sends an OTP to the user's registered email.
     *
     * @param otpRequest The request containing the email for which OTP needs to be sent.
     * @return ResponseEntity containing the OTP response.
     */
    @PostMapping("send-otp")
    public ResponseEntity<OtpResponse> sendOTP (@RequestBody OtpRequest otpRequest){
        return  authService.sendOtp(otpRequest);
    }

    /**
     * Resends the OTP to the user's registered email.
     *
     * @param otpRequest The request containing the email for which OTP needs to be resent.
     * @return ResponseEntity containing the OTP response.
     */
    @PostMapping("resend-otp")
    public ResponseEntity<OtpResponse> resendOTP(@RequestBody OtpRequest otpRequest){
        return authService.resendOTP(otpRequest);
    }

    /**
     * Verifies the OTP sent to the user's registered email.
     *
     * @param otpRequest The request containing the email and OTP to be verified.
     * @return ResponseEntity containing the OTP response.
     */
    @PostMapping("verify-OTP")
    public ResponseEntity<OtpResponse> verifyOTP(@RequestBody OtpRequest otpRequest){
        return authService.verifyOTP(otpRequest);
    }

    /**
     * Resets the user's password.
     *
     * @param passwordResetRequest The request containing the email and new password.
     * @return ResponseEntity containing the password reset response.
     */
    @PostMapping("reset-password")
    public ResponseEntity<PasswordResetResponse>resetPassword(@RequestBody PasswordResetRequest passwordResetRequest){
        return forgotPasswordService.resetPassword(passwordResetRequest);
    }
}
