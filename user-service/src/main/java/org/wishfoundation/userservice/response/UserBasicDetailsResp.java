package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.HealthStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicDetailsResp {

    private UUID id;
    private  String userName;
    private String phoneNumber;
    private String fullName;
    private HealthStatus status;

}
