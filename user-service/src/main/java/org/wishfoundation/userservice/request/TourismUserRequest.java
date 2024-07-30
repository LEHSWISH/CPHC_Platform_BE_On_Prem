package org.wishfoundation.userservice.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourismUserRequest {
    private String idtpId;
    private String phoneNumber;
    private String gender;
    private int age;
    private String emailId;
    private String tourStartDate;
    private String tourEndDate;
    private int tourDuration;
    private String address;
    private String disease;
    private String otherDisease;
    private int passengerId;
    private String fullName;
    private long yatraId;

}
