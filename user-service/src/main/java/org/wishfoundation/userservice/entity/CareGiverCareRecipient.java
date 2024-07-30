package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.RequestStatus;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "caregiver_carerecipient")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "childyatriPulseUsersDetails", "parentYatriPulseUsersDetails"})
public class CareGiverCareRecipient extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "parent_yatri_pulse_user_id", length = 50)
    private UUID parentYatriPulseUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_yatri_pulse_user_id", referencedColumnName = "id")
    @MapsId("parentYatriPulseUserId")
    private YatriPulseUsers parentYatriPulseUsersDetails;

    @Column(name = "child_yatri_pulse_user_id", length = 50)
    private UUID childYatriPulseUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_yatri_pulse_user_id", referencedColumnName = "id")
    @MapsId("childYatriPulseUserId")
    private YatriPulseUsers childyatriPulseUsersDetails;

    @Column(name = "request_status", length = 50)
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Column(name = "agreement_time")
    private java.time.Instant agreementTime;

    @Column(name = "request_accept_time")
    private java.time.Instant requestAcceptTime;

}
