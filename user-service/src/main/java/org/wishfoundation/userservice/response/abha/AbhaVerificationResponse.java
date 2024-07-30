package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaVerificationResponse {

    private String txnId;
    private String message;

    private String authResult;

    private String token;

    private int expiresIn;

    private String refreshToken;

    private int refreshExpiresIn;

    private List<ABHAProfile> accounts;


}
