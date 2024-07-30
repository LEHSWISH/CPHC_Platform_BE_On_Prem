package org.wishfoundation.userservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.userservice.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YatriDetailsResponse {
//	private String firstName;
//	private String lastName;
	private String fullName;
	private String emailId;
	private Gender gender;
	private String dateOfBirth;
	private String tourStartDate;
	private String tourEndDate;
	private int tourDuration;
	private int age;
	private String address;
	private String pinCode;
	private String state;
	private String district;
	private String phoneNumber;
}
