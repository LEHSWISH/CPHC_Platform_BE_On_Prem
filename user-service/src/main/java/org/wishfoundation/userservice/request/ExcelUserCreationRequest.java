package org.wishfoundation.userservice.request;


import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
public class ExcelUserCreationRequest {


    @JsonAlias("YatraId")
    private String yatraId;
    @JsonAlias("PassengerId")
    private String PassengerId;
    @JsonAlias("PassengerName")
    private String PassengerName;
    @JsonAlias("MobileNo")
    private String MobileNo;
    @JsonAlias("Age")
    private String Age;
    @JsonAlias("UniqueCode")
    private String UniqueCode;
    @JsonAlias("Gender")
    private String Gender;
    @JsonAlias("Address")
    private String Address;
    @JsonAlias("Email")
    private String Email;
    @JsonAlias("State Name")
    private String stateName;
    @JsonAlias("District Name")
    private String districtName;
    @JsonAlias("Tour start date")
    private String TourStartDate;
    @JsonAlias("Tour end date")
    private String TourEndDate;
}