package org.wishfoundation.abhaservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;
import org.wishfoundation.abhaservice.utils.Helper;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "care_context",uniqueConstraints = @UniqueConstraint(columnNames = { "care_context_id","document_path_id"}))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "district", "state","fhirDocumentList" })
public class CareContext extends AuditableEntity {

    /**
     *
     */
    private static final long serialVersionUID = -4630428315685866182L;

    // SET CARE_CONTEXT_ID
    @Id
    private String id;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "document_path_id")
    private List<String> documentPathId;

    @Column(name = "document_path")
    private List<String> documentPath;

    @Column(name = "hi_type")
    @Enumerated(EnumType.STRING)
    private MedicalDocumentType hiType;

    @Column(name = "document_description")
    private String documentsDescription;

    @Column(name = "abha_id")
    private String abhaId;

    @OneToMany(mappedBy = "careContext", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "care-context-fhir")
    private List<FhirDocuments> fhirDocumentList;

    public String getAbhaId() {
        return Helper.decrypt(abhaId);
    }

    public void setAbhaId(String abhaId) {
        this.abhaId = Helper.encrypt(abhaId);
    }

}
