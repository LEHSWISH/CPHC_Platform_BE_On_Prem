package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.CareType;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthDetailsResp {
    private UUID id;
    private String userName;
    private String password;
    private String phoneNumber;
    private CareType careType;
}
