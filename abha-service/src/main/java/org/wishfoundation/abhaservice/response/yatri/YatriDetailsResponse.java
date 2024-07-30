package org.wishfoundation.abhaservice.response.yatri;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YatriDetailsResponse {
//	private String firstName;
//	private String lastName;
	private String fullName;
	private String emailId;
	private String gender;
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
