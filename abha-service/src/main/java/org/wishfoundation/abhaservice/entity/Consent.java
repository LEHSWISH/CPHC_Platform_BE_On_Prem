package org.wishfoundation.abhaservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.wishfoundation.abhaservice.enums.ConsentStatus;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "consent")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "district", "state"})
public class Consent extends AuditableEntity {

    @Id
    private UUID id;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ConsentStatus status;
    @Column(name = "date_from")
    private String dateFrom;
    @Column(name = "date_to")
    private String dateTo;
    @Column(name = "data_erase_at")
    private String dataEraseAt;
    @Column(name = "hi_types")
    @Enumerated(EnumType.STRING)
    private List<MedicalDocumentType> hiTypes;
    @OneToMany(mappedBy = "consent", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonManagedReference("consent-artefacts")
    private List<ConsentArtefact> consentArtefacts;
}
