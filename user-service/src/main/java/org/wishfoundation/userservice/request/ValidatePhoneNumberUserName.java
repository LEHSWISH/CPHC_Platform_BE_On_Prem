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
public class ValidatePhoneNumberUserName {

    @Pattern(regexp="(^$|[0-9]{10})" , message = "error.validation.invalidPhoneNumber")
    @NotBlank
    String phoneNumber;

    @Pattern(regexp="^[a-zA-Z0-9]{5,20}$" , message = "error.validation.userNameMismatch")
    @NotBlank
    String userName;
}
