package org.wishfoundation.userservice.request;

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
public class OtpRequest {


    @Pattern(regexp="(^$|[0-9]{10})" , message = "error.validation.invalidPhoneNumber")
    @NotBlank
    String phoneNumber;
    String otp;

    @Pattern(regexp="^[a-zA-Z0-9]{5,20}$" , message = "error.validation.userNameMismatch")
    @NotBlank
    String userName;

    String templateKey;
}
