package org.wishfoundation.userservice.request.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wishfoundation.userservice.enums.AbhaAuthMethods;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class AbhaVerificationRequest {

    AbhaAuthMethods authMethod;


    String healthid;

    String value;

    String txnId;

    String otp;

}
