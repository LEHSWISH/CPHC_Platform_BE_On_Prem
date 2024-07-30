package org.wishfoundation.userservice.request.abha;

import org.wishfoundation.userservice.response.abha.ABHAProfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ABHAUserRequest {
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+\\s[a-zA-Z]+$", message = "User Name cannot contain special character")
	private String userName;

	@Pattern(regexp = "(^$|[0-9]{10})", message = "error.validation.invalidPhoneNumber")
	@NotBlank
	private String phoneNumber;

	private ABHAProfile abhaProfile;
}
