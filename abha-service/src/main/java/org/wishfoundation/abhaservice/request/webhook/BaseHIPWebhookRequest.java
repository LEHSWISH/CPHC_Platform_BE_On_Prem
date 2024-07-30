package org.wishfoundation.abhaservice.request.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.request.hip.Patient;
import org.wishfoundation.abhaservice.request.hiu.ConsentRequest;
import org.wishfoundation.abhaservice.request.hiu.HIUEntry;
import org.wishfoundation.abhaservice.request.hiu.HiRequest;
import org.wishfoundation.abhaservice.request.hiu.KeyMaterial;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseHIPWebhookRequest {
    public String requestId;
    public String timestamp;
    public Auth auth;
    public Object error;
    public Resp resp;
    public String transactionId;
    public Patient patient;
    private HIPConfirmation confirmation;


    // M3
    private ConsentRequest consentRequest;
    private HIUNotification notification;
    // M3 ON FETCH
    private ConsentFetchRequest consent;
    // M3 ON REQUEST
    public HiRequest hiRequest;

    // M3 DATA PUSH URL
    public List<HIUEntry> entries;
    public KeyMaterial keyMaterial;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Auth {
        // ON CONFRIM
        public String accessToken;
        public Patient patient;

        // ON INIT
        public String transactionId;
        public String mode;
        public Meta meta;
        public Validity validity;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public class Resp {
        public String requestId;
    }

}

