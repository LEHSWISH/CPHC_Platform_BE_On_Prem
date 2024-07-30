package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
	private String username;
	private String fullName;
	private String gender;
	private String email;
	private String dateOfBirth;
	private String address;
	private String district;
	private String state;
	private String pinCode;
	private String tourStartDate;
	private String tourEndDate;
	private int tourDuration;
	private String abhaNumber;
	private List<String> phrAddress;
	private MedicalsReportsResponse medicalsReports;
	private String phoneNumber;
}
