package org.wishfoundation.userservice.request;

import org.wishfoundation.userservice.enums.Gender;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YatriDetailsRequest {
	private String fullName;
//	private String firstName;
//	private String lastName;
	private String emailId;
	private String phoneNumber;
	private Gender gender;
	private String dateOfBirth;
	private String tourStartDate;
	private String tourEndDate;
	private int tourDuration;
	private java.time.Instant createdByTime;
	private java.time.Instant updatedByTime;
	private int age;
	private String address;
	private String pinCode;
	private String state;

	private String district;




}
