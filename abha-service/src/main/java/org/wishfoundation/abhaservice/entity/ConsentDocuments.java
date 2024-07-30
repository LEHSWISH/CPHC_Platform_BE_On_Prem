package org.wishfoundation.abhaservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.wishfoundation.abhaservice.enums.ConsentStatus;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.utils.Helper;
import org.wishfoundation.chardhamcore.entity.AuditableEntity;

import java.util.List;
import java.util.UUID;


/*
* THIS ENTITY ONLY USED FOR HIT OPS.
* */

@Getter
@Setter
@Entity
@Table(name = "consent_documents")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "district", "state"})
public class ConsentDocuments extends AuditableEntity {

    @Id
    private UUID id;
    @Column(name = "abha_address")
    private String abhaAddress;
    @Column(name = "hip_name")
    private String hipName;
    @Column(name = "hip_id")
    private String hipId;
    @Column(name = "hi_types")
    @Enumerated(EnumType.STRING)
    private List<MedicalDocumentType> hiTypes;
    @Column(name = "access_mode")
    private String accessMode;
    @Column(name = "date_from")
    private String dateFrom;
    @Column(name = "date_to")
    private String dateTo;
    @Column(name = "data_erase_at")
    private String dataEraseAt;
    @Column(name = "patient_id")
    private List<String> patientIds;
    @Column(name = "care_context_id")
    private List<String> careContextIds;
    @Column(name = "transaction_request_id",unique = true)
    private UUID transactionRequestId;
    @Column(name = "transaction_id",unique = true)
    private UUID transactionId;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ConsentStatus status;
    @Column(name = "data_push_url")
    private String dataPushUrl;
    @Column(name = "crypto_alg")
    private String cryptoAlg;
    @Column(name = "curve")
    private String curve;
    @Column(name = "parameters")
    private String parameters;
    @Column(name = "key_value" , length = 500)
    private String keyValue;
    @Column(name = "nonce")
    private String nonce;


    public String getAbhaAddress() {
        return Helper.decrypt(abhaAddress);
    }

    public void setAbhaAddress(String abhaAddress) {
        this.abhaAddress = Helper.encrypt(abhaAddress);
    }
}
