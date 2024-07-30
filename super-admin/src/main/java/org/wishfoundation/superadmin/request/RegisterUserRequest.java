package org.wishfoundation.superadmin.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.superadmin.enums.Roles;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private List<Roles> roles;
}
