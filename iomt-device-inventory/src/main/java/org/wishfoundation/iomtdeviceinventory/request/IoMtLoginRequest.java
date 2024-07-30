package org.wishfoundation.iomtdeviceinventory.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class IoMtLoginRequest {
    private String userServiceToken;
    private String entityId;

}
