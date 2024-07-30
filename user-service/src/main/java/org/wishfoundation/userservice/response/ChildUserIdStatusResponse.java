package org.wishfoundation.userservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.wishfoundation.userservice.enums.RequestStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ChildUserIdStatusResponse {
    private UUID childYatriPulseUserId;
    private RequestStatus requestStatus;
}
