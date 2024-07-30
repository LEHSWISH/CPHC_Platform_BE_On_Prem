package org.wishfoundation.superadmin.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.superadmin.request.LoginRequest;
import org.wishfoundation.superadmin.request.OtpRequest;
import org.wishfoundation.superadmin.request.RegisterUserRequest;
import org.wishfoundation.superadmin.response.LoginResponse;
import org.wishfoundation.superadmin.response.OtpResponse;

/**
 * This interface defines the methods for authentication related operations.
 */
public interface AuthService {

    /**
     * Performs user login.
     *
     * @param loginRequest The request object containing user credentials.
     * @param sessionId The session ID for the current user.
     * @return ResponseEntity containing the login response.
     */
    ResponseEntity<LoginResponse> login(LoginRequest loginRequest, String sessionId);

    /**
     * Registers a new user.
     *
     * @param registerUserRequest The request object containing user registration details.
     * @return ResponseEntity indicating success or failure.
     */
    ResponseEntity<Void> register(RegisterUserRequest registerUserRequest);

    /**
     * Sends an OTP to the user's registered mobile number.
     *
     * @param otpRequest The request object containing the user's mobile number.
     * @return ResponseEntity containing the OTP response.
     */
    ResponseEntity<OtpResponse> sendOtp(OtpRequest otpRequest);

    /**
     * Resends the OTP to the user's registered mobile number.
     *
     * @param otpRequest The request object containing the user's mobile number.
     * @return ResponseEntity containing the OTP response.
     */
    ResponseEntity<OtpResponse> resendOTP(OtpRequest otpRequest);

    /**
     * Verifies the OTP sent to the user's registered mobile number.
     *
     * @param otpRequest The request object containing the user's mobile number and the OTP.
     * @return ResponseEntity containing the OTP response.
     */
    ResponseEntity<OtpResponse> verifyOTP(OtpRequest otpRequest);
}
