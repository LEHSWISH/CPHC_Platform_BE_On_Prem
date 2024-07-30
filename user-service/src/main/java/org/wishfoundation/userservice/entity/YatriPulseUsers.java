package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.CareType;
import org.wishfoundation.userservice.enums.CreationType;
import org.wishfoundation.userservice.enums.GovernmentIdType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "yatri_pulse_users", uniqueConstraints = @UniqueConstraint(columnNames = {"user_name", "phone_number"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "yatriDetails", "medicalsReports", "abhaUserDetails", "documentsPath", "tourismUserInfo", "evaidyaUserDetailsEntity", "vitalsRecord"})
public class YatriPulseUsers extends AuditableEntity {

    /**
     *
     */
    private static final long serialVersionUID = -4630428315685866182L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_name", unique = true, length = 50)
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "license_agreement")
    private boolean licenseAgreement;

    @Column(name = "license_agreement_time")
    private java.time.Instant licenseAgreementTime;

    @Column(name = "abha_user_id", length = 50)
    private UUID abhaUserId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "abha_user_id", referencedColumnName = "id")
    @MapsId("abhaUserId")
    private ABHAUserDetails abhaUserDetails;

    @Column(name = "government_id_type", length = 30)
    @Enumerated(EnumType.STRING)
    private GovernmentIdType governmentIdType;

    @Column(name = "government_id", unique = true, length = 50)
    private String governmentId;

    @OneToMany(mappedBy = "yatriPulseUsers", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonManagedReference("yatriPulseUser-documentsPath")
    private List<DocumentsPath> documentsPath;

    @Column(name = "tourism_id", length = 50)
    private UUID tourismId;

    @Column(name = "yatri_details_id", length = 50)
    private UUID yatriDetailsId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "yatri_details_id", referencedColumnName = "id")
    @MapsId("yatriDetailsId")
    private YatriDetails yatriDetails;

    @Column(name = "medicals_reports_id", length = 50)
    private UUID medicalsReportsId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "medicals_reports_id", referencedColumnName = "id")
    @MapsId("medicalsReportsId")
    private MedicalsReports medicalsReports;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "tourism_id", referencedColumnName = "id")
    @MapsId("tourismId")
    private TourismUserInfo tourismUserInfo;

    @Column(name = "evaidya_user_details_entity_id", length = 50)
    private UUID evaidyaUserDetailsEntityId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "evaidya_user_details_entity_id", referencedColumnName = "id")
    @MapsId("evaidyaUserDetailsEntityId")
    private EvaidyaUserDetails evaidyaUserDetails;

    @OneToMany(mappedBy = "yatriPulseUsers", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonManagedReference("yatriPulseUser-vitalsRecord")
    private List<VitalsRecord> vitalsRecords;

    @Column(name = "care_type", length = 20)
    @Enumerated(EnumType.STRING)
    private CareType careType;

    @Column(name="creation_type", length =20)
    @Enumerated(EnumType.STRING)
    private CreationType creationType;

}
