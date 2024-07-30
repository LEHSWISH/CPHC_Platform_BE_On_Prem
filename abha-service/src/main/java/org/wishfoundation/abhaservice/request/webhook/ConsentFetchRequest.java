package org.wishfoundation.abhaservice.request.webhook;

import lombok.Data;
import org.wishfoundation.abhaservice.enums.ConsentStatus;
import org.wishfoundation.abhaservice.request.hiu.ConsentRequest;

@Data
public class ConsentFetchRequest {
    public ConsentStatus status;
    public ConsentRequest consentDetail;
    public String signature;
}
