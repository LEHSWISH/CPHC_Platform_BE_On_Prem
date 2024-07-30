package org.wishfoundation.superadmin.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PasswordResetRequest {

    private String email;
    private String otp;
    private String templateKey;
    private String password;


}
