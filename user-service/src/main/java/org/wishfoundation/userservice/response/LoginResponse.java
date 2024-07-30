package org.wishfoundation.userservice.response;

import org.wishfoundation.userservice.request.YatriPulseUserRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse
{
    private String token;
    private YatriPulseUserRequest yatri;
    private String message;
    private String phoneNumber;
    private String userName;

}
