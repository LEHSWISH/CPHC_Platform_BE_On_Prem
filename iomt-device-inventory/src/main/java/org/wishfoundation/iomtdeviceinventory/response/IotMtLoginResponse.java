package org.wishfoundation.iomtdeviceinventory.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IotMtLoginResponse {
    private String token;
    private  String phoneNumber;
    private String userName;
}
