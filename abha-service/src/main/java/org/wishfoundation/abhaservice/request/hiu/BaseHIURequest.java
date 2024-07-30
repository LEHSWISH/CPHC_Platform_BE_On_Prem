package org.wishfoundation.abhaservice.request.hiu;

import lombok.Data;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.request.hip.Patient;

import java.util.List;
import java.util.UUID;

@Data
public class BaseHIURequest {
    private String requestId;
    private String timestamp;
    public ConsentRequest consent;

    // FOR FETCH CONSENT
    private String consentId;

    // FOR DOC REQUEST
    private HiRequest hiRequest;

    // FROM PORTAL
    private DateRange dateRange;
    private String dataEraseAt;
    private List<UUID> consentIds;
    public List<MedicalDocumentType> hiTypes;


    private String userName;
    private UUID onRequestKey;

}
