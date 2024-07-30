package org.wishfoundation.userservice.request.abha;

import lombok.Data;

@Data
public class BaseDiscoveryRequest {
    private String abhaId;
    private String name;
    private String gender;
    private int yearOfBirth;
    private String phoneNumber;
}
