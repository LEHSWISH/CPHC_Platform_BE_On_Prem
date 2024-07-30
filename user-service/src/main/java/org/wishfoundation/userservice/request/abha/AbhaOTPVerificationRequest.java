package org.wishfoundation.userservice.request.abha;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaOTPVerificationRequest {
    private AuthData authData;
    private Consent consent;
    private List<String> scope;
}

