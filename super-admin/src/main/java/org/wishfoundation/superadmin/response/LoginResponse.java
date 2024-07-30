package org.wishfoundation.superadmin.response;

import lombok.*;
import org.wishfoundation.superadmin.entity.UserAccounts;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private UserAccounts userAccounts;
    private String message;
    private String phoneNumber;
    private String userName;

}
