package org.wishfoundation.userservice.request.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Otp {
    private String timeStamp;
    private String txnId;
    private String otpValue;
    private String mobile;
}
