package org.wishfoundation.userservice.request.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaRegistrationRequest {
    String aadhaar;
    String otp;

    String txnId;

    String mobile;

    boolean consent;

    String consentVersion;

    // USED IN ABHA ADDRESS
    String abhaAddress;
    Integer preferred = 1;

    // USED IN ABHA CARD & QR
    @JsonProperty("abhaToken")
    private String abhaToken;
    private String authType = "v3";
    @JsonProperty("ABHANumber")
    private String ABHANumber;
    private String firstName;
    private String apiRouteSuffix;
    private String host;
    private String extenstion;

    // FOR DEMO API
    private String aadharNumber;
    private String gender;
    private String dateOfBirth;
    private String stateCode;
    private String districtCode;
    private String name;
    private String benefitName = "eSwasthya Dham";
    private String mobileNumber;

    // FOR ABHA ADDRESS AUTH VIA MOBILE
    private String authMethod;
    private String healthid;


    // check for rate limit
    private String templateKey;


    // For binding exception in recursion
    boolean binded;

}
