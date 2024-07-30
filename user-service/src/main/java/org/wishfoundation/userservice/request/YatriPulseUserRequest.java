package org.wishfoundation.userservice.request;

import jakarta.persistence.Column;
import org.wishfoundation.userservice.entity.DocumentsPath;
import org.wishfoundation.userservice.enums.HealthStatus;
import org.wishfoundation.userservice.enums.MedicalDocumentType;
import org.wishfoundation.userservice.enums.GovernmentIdType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.wishfoundation.userservice.response.abha.ABHAProfile;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class YatriPulseUserRequest {

    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "User Name cannot contain special character")
    private String userName;
    private String password;
    private String sessionId;

    @Pattern(regexp = "(^$|[0-9]{10})", message = "error.validation.invalidPhoneNumber")
    @NotBlank
    private String phoneNumber;

    private String otp;
    private boolean licenseAgreement;
    private java.time.Instant licenseAgreementTime;
    private String abhaNumber;
    private MedicalDocumentType documentType;
    private String hospitalLabName;
    private String visitPurpose;
    private List<DocumentsPathRequest> documentsPath;
    private GovernmentIdType governmentIdType;
    private String governmentId;
    private String idtpId;
    private String idypId;
    private YatriDetailsRequest yatriDetails;
    private MedicalsReportsRequest medicalsReports;

    private String templateKey;

    private String evaidyaUserId;
    private String name;
    private String dob;
    private HealthStatus status;

    private ABHAProfile abhaProfile;
    private VitalsRecordRequest vitalsRecord;


    // FOR ABHA M2 FLOW
    private String careContextId;
    private List<DocumentsPath> documentsPathEntity;
}
