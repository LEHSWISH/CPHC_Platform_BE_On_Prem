package org.wishfoundation.userservice.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.request.abha.ABHAUserRequest;
import org.wishfoundation.userservice.request.abha.AbhaRegistrationRequest;
import org.wishfoundation.userservice.response.abha.ABHAProfile;
import org.wishfoundation.userservice.response.abha.AbhaOTPVerificationResponse;
import org.wishfoundation.userservice.response.abha.GeneralAbhaResponse;
import org.wishfoundation.userservice.response.abha.GetABHAResponse;
import org.wishfoundation.userservice.service.AbhaFetchDetails;
import org.wishfoundation.userservice.service.AbhaRegistrationServiceImpl;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/abha/registration")
public class AbhaRegistrationController {

    private final AbhaRegistrationServiceImpl abhaService;

    @Autowired
    private AbhaFetchDetails abaAbhaFetchDetails;

    //when mobile number linked with aadhaar
    @PostMapping("/aadhaar/generateAadhaarOTP")
    public GeneralAbhaResponse generateOTP(@RequestBody AbhaRegistrationRequest request) {
        request.setTemplateKey("generateOTP");
        return abhaService.generateOTP(request);
    }

    @PostMapping("/aadhaar/verifyAadhaarOTP")
    public GeneralAbhaResponse verifyOTP(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.verifyOTP(request);
    }

    @PostMapping("/aadhaar/generateMobileOTP")
    public GeneralAbhaResponse generateMobileOTP(@RequestBody AbhaRegistrationRequest request) {
        request.setTemplateKey("generateMobileOTP");
        return abhaService.generateMobileOTP(request);
    }

    @PostMapping("/aadhaar/verifyMobileOTP")
    public GeneralAbhaResponse verifyMobileOTP(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.verifyMobileOTP(request);
    }

//    @PostMapping("/aadhaar/createHealthIdByAadhaar")
//    public AbhaUserDetailsResponse createHealthIdByAadhaar(@RequestBody AbhaRegistrationRequest request){
//        return abhaService.createHealthIdByAadhaar(request);
//    }

    @PostMapping("/save/abha-details")
    public ResponseEntity<Void> saveAbhaDetails(@RequestBody AbhaOTPVerificationResponse abhaUserRequest) {
        return abhaService.saveAbhaDetails(abhaUserRequest);
    }

    @PostMapping("/fetch/abha-details")
    public ABHAProfile fetchAbhaDetails(@RequestBody ABHAUserRequest abhaUserRequest) {
        return abhaService.fetchAbhaDetails(abhaUserRequest);
    }

    @PostMapping("/fetch/abha-profile")
    public GetABHAResponse fetchAbhaProfile(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.fetchAbhaProfile(request);
    }


    @PostMapping(path = "/fetch/abha-card")
    public GeneralAbhaResponse fetchAbhaCard(@RequestBody AbhaRegistrationRequest request) {
//        if (request.getAuthType().equalsIgnoreCase("v2"))
        return abaAbhaFetchDetails.fetchAbhaCardV2(request);
//        return abaAbhaFetchDetails.fetchAbhaCard(request);
    }

    @PostMapping(path = "/fetch/abha-qr")
    public GeneralAbhaResponse fetchAbhaQR(@RequestBody AbhaRegistrationRequest request) {
        if (request.getAuthType().equalsIgnoreCase("v2"))
            return abaAbhaFetchDetails.fetchAbhaQRV2(request);

        return abaAbhaFetchDetails.fetchAbhaQR(request);
    }

    @PostMapping(path = "/fetch/abha-card-pdf")
    public GeneralAbhaResponse fetchAbhaPdf(@RequestBody AbhaRegistrationRequest request) {
        return abaAbhaFetchDetails.fetchAbhaPdf(request);
    }


    @PostMapping("/fetch/abha-address-suggestion")
    public GeneralAbhaResponse addressSuggestion(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.addressSuggestion(request);
    }

    @PostMapping("/create/address")
    public GeneralAbhaResponse createAddress(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.createAddress(request);
    }

    @PostMapping("/auth-demo")
    public GeneralAbhaResponse createAbhaIdByDemo(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.createAbhaIdByDemo(request);
    }

    @PostMapping("/save-abha-user-details")
    public ResponseEntity<ABHAUserDetails> saveABHAUserDetails(@RequestBody AbhaRegistrationRequest request) {
        return abhaService.saveABHAUserDetails(request);
    }


}
