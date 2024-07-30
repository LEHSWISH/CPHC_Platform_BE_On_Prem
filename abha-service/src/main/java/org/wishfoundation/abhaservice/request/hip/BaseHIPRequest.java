package org.wishfoundation.abhaservice.request.hip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.request.DocumentsPath;
import org.wishfoundation.abhaservice.request.Resp;
import org.wishfoundation.abhaservice.request.webhook.HIPNotification;
import org.wishfoundation.abhaservice.response.yatri.YatriDetailsResponse;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class BaseHIPRequest {

    // portal request
    private String visitPurpose;
    private List<DocumentsPath> documentsPathEntity;
    private MedicalDocumentType documentType;
    private String careContextId;

    private String requestId;
    private String timestamp;
    private HIPQuery query;
    private String transactionId;

    // FOR ADD CARE CONTEXT
    private Link link;
    private Patient patient; // set on callback if patient detail need.

    // ON DISCOVERY
    private Resp resp;
    private Object error;
    // NOTIFY API (M2)(DEEP LINK)
    private HIPNotification notification;

    //INTERNAL REQUEST FOR NOTIFY API.
    private YatriDetailsResponse yatriDetails;

}
