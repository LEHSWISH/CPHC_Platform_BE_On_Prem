package org.wishfoundation.healthservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class YatriPulseUserRequest {

	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+\\s[a-zA-Z]+$", message = "User Name cannot contain special character")
	private String userName;

	@Pattern(regexp = "(^$|[0-9]{10})", message = "error.validation.invalidPhoneNumber")
	@NotBlank
	private String phoneNumber;

	private MedicalsReportsRequest medicalsReports;
}
