package org.wishfoundation.userservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.userservice.request.abha.AbhaRegistrationRequest;
import org.wishfoundation.userservice.request.abha.AbhaVerificationRequest;
import org.wishfoundation.userservice.response.abha.AbhaOTPVerificationResponse;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;
import org.wishfoundation.userservice.response.abha.GetABHAResponse;
import org.wishfoundation.userservice.service.AbhaVerificationServiceImpl;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/abha/verification")
public class AbhaVerificationController {

    private final AbhaVerificationServiceImpl abhaService;

    // ABHA ADDRESS VIA MOBILE LOGIN
    @PostMapping("/abha-address/mobile-login-otp")
    public GeneralAbhaResponse abhaAddressMobileLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("abhaAddressMobileLoginOtp");
        return abhaService.abhaAddressMobileLoginOtp(request);
    }

    @PostMapping("/abha-address/mobile-login-otp-verify")
    public GeneralAbhaResponse abhaAddressMobileOtpVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.abhaAddressMobileOtpVerify(request);
    }

    // ABHA ADDRESS VIA AADHAAR LOGIN
    @PostMapping("/abha-address/login-otp")
    public GeneralAbhaResponse abhaAddressLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("abhaAddressLoginOtp");
        return abhaService.abhaAddressLoginOtp(request);
    }

    @PostMapping("/abha-address/login-otp-verify")
    public GeneralAbhaResponse abhaAddressOtpVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.abhaAddressOtpVerify(request);
    }

    // ABHA ID VIA AADHAAR LOGIN
    @PostMapping("/abha-id/login-otp")
    public GeneralAbhaResponse abhaIdLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("abhaIdLoginOtp");
        return abhaService.abhaIdLoginOtp(request);
    }

    @PostMapping("/abha-id/login-verify")
    public GeneralAbhaResponse abhaIdLoginVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.abhaIdLoginVerify(request);
    }

    // ABHA ID VIA MOBILE LOGIN
    @PostMapping("/abha-id/mobile-login-otp")
    public GeneralAbhaResponse abhaIdMobileLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("abhaIdMobileLoginOtp");
        return abhaService.abhaIdMobileLoginOtp(request);
    }

    @PostMapping("/abha-id/mobile-login-verify")
    public GeneralAbhaResponse abhaIdMobileLoginVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.abhaIdMobileLoginVerify(request);
    }


    // FOR MOBILE LOGIN
    @PostMapping("/mobile-login-otp")
    public GeneralAbhaResponse mobileLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("mobileLoginOtp");
        return abhaService.mobileLoginOtp(request);
    }

    @PostMapping("/mobile-login-verify")
    public AbhaOTPVerificationResponse mobileLoginVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.mobileLoginVerify(request);
    }

    @PostMapping("/user-verify")
    public GeneralAbhaResponse userVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.userVerify(request);
    }
    // FORGET ABHA VIA MOBILE
    @PostMapping("/forget-abha-otp")
    public GeneralAbhaResponse retriveAbhaIdOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("retriveAbhaIdOtp");
        return abhaService.mobileLoginOtp(request);
    }

    @PostMapping("/forget-abha-verify")
    public AbhaOTPVerificationResponse retriveAbhaIdVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.mobileLoginVerify(request);
    }

    @PostMapping("/forget-user-verify")
    public GeneralAbhaResponse retriveUserVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.userVerify(request);
    }

    @PostMapping("/aadhaar-login-otp")
    public GeneralAbhaResponse aadhaarLoginOtp(@RequestBody AbhaRegistrationRequest request){
        request.setTemplateKey("aadhaarLoginOtp");
        return abhaService.aadhaarLoginOtp(request);
    }

    @PostMapping("/aadhaar-login-verify")
    public GeneralAbhaResponse adhaarLoginVerify(@RequestBody AbhaRegistrationRequest request){
        return abhaService.aadhaarLoginVerify(request);
    }

    //Todo:NO resend OTP api

//    @PostMapping("/resendMobileOTP")
//    public AbhaVerificationResponse resendMobileOTP(@RequestBody AbhaVerificationRequest request){
//        return abhaService.resendMobileOTP(request);
//    }



}
