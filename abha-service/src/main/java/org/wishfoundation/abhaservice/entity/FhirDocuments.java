package org.wishfoundation.abhaservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Table(name = "fhir_documents")
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "careContext" })
public class FhirDocuments extends AuditableEntity{

    private static final long serialVersionUID = -4630428315685866192L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "care_context_id", length = 50)
    private String careContextId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_context_id", referencedColumnName = "id")
    @MapsId("careContextId")
    @JsonBackReference(value = "care-context-fhir")
    private CareContext careContext;

    @Column(name = "path")
    private String path;

}
