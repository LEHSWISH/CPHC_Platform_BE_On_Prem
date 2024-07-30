package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaOTPVerificationResponse {
    private String message;
    private String txnId;
    private Tokens tokens;
    @JsonProperty("ABHAProfile")
    private ABHAProfile abhaProfile;
    private Boolean isNew;
    private String authResult;
    private List<ABHAProfile> accounts;
    private String token;
    private int expiresIn;
    private String refreshToken;
    private int refreshExpiresIn;
    private String requestMobile;

}

