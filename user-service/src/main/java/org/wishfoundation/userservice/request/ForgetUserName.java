package org.wishfoundation.userservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.wishfoundation.userservice.enums.GovernmentIdType;

@Data
public class ForgetUserName {

	@Pattern(regexp = "(^$|[0-9]{10})", message = "error.validation.invalidPhoneNumber")
	@NotBlank
	private String phoneNumber;

	private String abhaNumber;
	private GovernmentIdType governmentIdType;
	private  String governmentId;

	private String otp;

}
