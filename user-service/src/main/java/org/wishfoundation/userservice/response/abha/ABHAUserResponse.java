package org.wishfoundation.userservice.response.abha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ABHAUserResponse {
    @JsonProperty("ABHANumber")
    private String abhaNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String emailId;
    private List<String> phrAddress;
    private String address;
    private String districtCode;
    private String stateCode;
    private String districtName;
    private String stateName;
    private String pinCode;
    private String abhaType;
    private String abhaStatus;

    private boolean abhaVerified;

}
