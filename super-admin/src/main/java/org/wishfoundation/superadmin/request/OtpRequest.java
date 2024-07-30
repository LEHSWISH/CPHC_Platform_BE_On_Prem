package org.wishfoundation.superadmin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Length;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$" , message = "Invalid email address. Please enter a proper email ID.")
    @NotBlank
    private String emailId;
    private String otp;
    private String templateKey;

    private final int  length=6;

}
