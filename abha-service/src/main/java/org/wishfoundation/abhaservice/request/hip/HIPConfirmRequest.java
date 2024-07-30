package org.wishfoundation.abhaservice.request.hip;

import lombok.Data;

@Data
public class HIPConfirmRequest extends BaseHIPRequest{
    private Credential credential;
}
