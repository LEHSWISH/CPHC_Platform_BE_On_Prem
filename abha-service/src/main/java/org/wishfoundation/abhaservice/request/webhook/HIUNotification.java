package org.wishfoundation.abhaservice.request.webhook;

import lombok.Data;
import org.wishfoundation.abhaservice.entity.ConsentArtefact;
import org.wishfoundation.abhaservice.request.hiu.ConsentRequest;

import java.util.List;

@Data
public class HIUNotification {
    public String consentRequestId;
    public String status;
    public List<ConsentArtefact> consentArtefacts;

    // M2 notify
    private ConsentRequest consentDetail;
    private String consentId;
}


