package org.wishfoundation.abhaservice.request.webhook;

import lombok.Data;
import org.wishfoundation.abhaservice.request.hip.HIPRequester;
import org.wishfoundation.abhaservice.request.hiu.ConsentRequest;

@Data
public class HIPNotification {
    public String status;
    public String consentId;
    public ConsentRequest consentDetail;
    public String signature;

    // NOTIFY API (M2)(DEEP LINK)
    public String phoneNo;
    public HIPRequester hip;
}
