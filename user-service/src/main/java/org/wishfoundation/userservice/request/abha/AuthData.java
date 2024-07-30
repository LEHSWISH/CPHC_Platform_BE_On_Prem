package org.wishfoundation.userservice.request.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthData {
    private List<String> authMethods;
    private Otp otp;
}
