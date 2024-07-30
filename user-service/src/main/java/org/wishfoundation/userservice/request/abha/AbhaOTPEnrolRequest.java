package org.wishfoundation.userservice.request.abha;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaOTPEnrolRequest {
    private List<String> scope;
    private String loginHint;
    private String loginId;
    private String otpSystem;
    private String txnId;
    private AuthData authData;

    // FOR ABHA ADDRESS AUTH VIA MOBILE
    private String otp;
}

