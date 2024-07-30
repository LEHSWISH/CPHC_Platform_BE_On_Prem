package org.wishfoundation.userservice.response.abha;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ABHAProfile {
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private String dob;
    private String gender;
    private String photo;
    private String mobile;
    private String email;
    private List<String> phrAddress;
    private String address;
    private String districtCode;
    private String stateCode;
    private String pinCode;
    private String abhaType;

    @JsonProperty("ABHANumber")
    private String abhaNumber;
    private String abhaStatus;
    private String stateName;
    private String districtName;
    private String preferredAbhaAddress;
    private String kycVerified;
    private String profilePhoto;
    private String status;
    private String verifiedStatus;
    private String verificationType;

}
