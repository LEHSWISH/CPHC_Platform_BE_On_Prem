package org.wishfoundation.userservice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourismUserApiResponse {

    @JsonProperty("UniqueCode")
    private String uniqueCode;

    @JsonProperty("MobileNo")
    private String mobileNo;

    @JsonProperty("Gender")
    private String gender;

    @JsonProperty("Age")
    private int age;

    @JsonProperty("EmailId")
    private String emailId;

    @JsonProperty("TourDuration")
    private String tourDuration;

    @JsonProperty("TravelDate")
    private String travelDate;

    @JsonProperty("Address")
    private String address;
    @JsonProperty("Disease")
    private String disease;

    @JsonProperty("Other Disease")
    private String otherDisease;
    @JsonProperty("PassengerId")
    private int passengerId;
    @JsonProperty("PassengerName")
    private String passengerName;
    @JsonProperty("YatraId")
    private long yatraId;

}
