package org.wishfoundation.abhaservice.request.webhook;

import lombok.Data;

@Data
public class HIPConfirmation {
    public String token;
    public String linkRefNumber;
}
