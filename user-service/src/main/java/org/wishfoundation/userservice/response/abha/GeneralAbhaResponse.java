package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GeneralAbhaResponse {
    private String txnId;
    private String message;

    @JsonProperty("ABHANumber")
    private String abhaNumber;
    private String mobile;

    private Tokens tokens;
    public String firstName;
    public String authType;

    private boolean mobileNumberMatched;
    // ABHA ADDRESS
    private List<String> abhaAddressList;
    private String preferredAbhaAddress;
    private String healthIdNumber;
    private String preSignedUrl;
    private String fileBase64;
    private boolean isNew;
}
