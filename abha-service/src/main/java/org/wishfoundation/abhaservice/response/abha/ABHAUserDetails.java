package org.wishfoundation.abhaservice.response.abha;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ABHAUserDetails {
    private UUID id;
    private String abhaNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String imagePath;
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
}