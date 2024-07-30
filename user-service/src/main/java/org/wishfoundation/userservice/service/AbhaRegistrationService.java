package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.request.abha.AbhaRegistrationRequest;
import org.wishfoundation.userservice.response.abha.AbhaOTPVerificationResponse;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;

public interface AbhaRegistrationService {

    GeneralAbhaResponse generateOTP(AbhaRegistrationRequest request);

    GeneralAbhaResponse verifyOTP(AbhaRegistrationRequest request);

    GeneralAbhaResponse generateMobileOTP(AbhaRegistrationRequest request);

    GeneralAbhaResponse verifyMobileOTP(AbhaRegistrationRequest request);

    ResponseEntity<Void> saveAbhaDetails(AbhaOTPVerificationResponse abhaOTPVerificationResponse);

    ResponseEntity<ABHAUserDetails> saveABHAUserDetails(AbhaRegistrationRequest request);

//    AbhaUserDetailsResponse createHealthIdByAadhaar(AbhaRegistrationRequest request);
}
