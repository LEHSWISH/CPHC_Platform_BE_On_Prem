package org.wishfoundation.userservice.service;

import org.wishfoundation.userservice.request.abha.AbhaVerificationRequest;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;

public interface AbhaVerificationService {

    GeneralAbhaResponse generateMobileOTP(AbhaVerificationRequest request);

    GeneralAbhaResponse verifyMobileOTP(AbhaVerificationRequest request);

//    AbhaVerificationResponse resendMobileOTP(AbhaVerificationRequest request);

}
