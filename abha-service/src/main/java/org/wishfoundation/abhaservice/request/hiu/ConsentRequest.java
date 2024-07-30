package org.wishfoundation.abhaservice.request.hiu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.abhaservice.entity.CareContext;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.request.hip.CareContextRequset;
import org.wishfoundation.abhaservice.request.hip.HIPRequester;
import org.wishfoundation.abhaservice.request.hip.Patient;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {
    public HIUPurpose purpose;
    public Patient patient;
    public HiuRequest hiu;
    public HIURequester requester;
    public List<MedicalDocumentType> hiTypes;
    public HIUPermission permission;


    //M2 notify
    public String schemaVersion;

    // FOR CALLBACK API.
    private String id;

    // ON FETCH
    public String consentId;
    private HIPRequester hip;
    public String createdAt;
    public String lastUpdated;
    public List<CareContextRequset> careContexts;
    public HIUConsentManager consentManager;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class HIUConsentManager{
        public String id;
    }

}
