package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbhaUserDetailsResponse {
    @JsonProperty("ABHANumber")
    private String abhaNumber;
    private String preferredAbhaAddress;
    private String mobile;
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private String yearOfBirth;
    private String dayOfBirth;
    private String monthOfBirth;
    private String gender;
    private String email;
    private String profilePhoto;
    private String status;
    private String stateCode;
    private String districtCode;
    private String subDistrictCode;
    private String villageCode;
    private String townCode;
    private String wardCode;
    private String pincode;
    private String address;
    private String kycPhoto;
    private String stateName;
    private String districtName;
    private String subdistrictName;
    private String villageName;
    private String townName;
    private String wardName;
    private List<String> authMethods;
    private Map<String, Object> tags;
    private boolean kycVerified;
    private String verificationStatus;
    private String verificationType;
    private boolean emailVerified;

}
