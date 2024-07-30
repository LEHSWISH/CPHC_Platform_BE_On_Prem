package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class    GetABHAResponse {
    @JsonProperty("ABHANumber")
    public String aBHANumber;
    public String healthIdNumber;
    public String healthId;
    public String preferredAbhaAddress;
    public String mobile;
    public String firstName;
    public String middleName;
    public String lastName;
    public String name;
    public String yearOfBirth;
    public String dayOfBirth;
    public String monthOfBirth;
    public String gender;
    public String email;
    public String profilePhoto;
    public String status;
    public String stateCode;
    public String districtCode;
    public String subDistrictCode;
    public String villageCode;
    public String townCode;
    public String wardCode;
    public String pincode;
    public String address;
    public String kycPhoto;
    public String stateName;
    public String districtName;
    public String subdistrictName;
    public String villageName;
    public String townName;
    public String wardName;
    public List<String> authMethods;
    public Object tags;
    public boolean kycVerified;
    public String verificationStatus;
    public String verificationType;
    public String emailVerified;
    public Tokens jwtResponse;
    public List<String> phrAddress;
    public String abhaType;
    private String abhaStatus;
    private String message;

    public String getABHANumber(){
        return StringUtils.hasLength(this.healthIdNumber) ? this.healthIdNumber : this.aBHANumber;
    }

    public List<String> getPhrAddress() {
        List<String> list = Arrays.asList(StringUtils.hasLength(this.preferredAbhaAddress) ? this.preferredAbhaAddress : this.healthId);
        if (ObjectUtils.isEmpty(this.phrAddress)) {
            return list;
        } else {
            this.phrAddress.addAll(list);
            return this.phrAddress;
        }
    }

    public String getEmail() {
        String email = StringUtils.hasLength(this.email) ? this.email : StringUtils.hasLength(this.emailVerified) ? emailVerified.contains("@") ? this.emailVerified : "" : "";
       return email;
    }
}


