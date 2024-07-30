package org.wishfoundation.abhaservice.request.hiu;

import lombok.Data;

@Data
public class HiRequest {
    public ConsentRequest consent;
    public DateRange dateRange;
    public String dataPushUrl;
    public KeyMaterial keyMaterial;

    // M3 ON REQUEST
    public String transactionId;
    public String sessionStatus;
}
