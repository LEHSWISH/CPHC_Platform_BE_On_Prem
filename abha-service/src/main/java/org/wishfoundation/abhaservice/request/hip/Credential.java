package org.wishfoundation.abhaservice.request.hip;

import lombok.Data;

@Data
public class Credential {
    private DemographicCredential demographic;
    private String authCode;
}