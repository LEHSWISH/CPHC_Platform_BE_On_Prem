package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.userservice.enums.HealthStatus;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "evaidya_user_details")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsersDetails" })
public class EvaidyaUserDetails extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "evaidya_user_id", length = 50)
    private String evaidyaUserId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "dob", length = 50)
    private String dob;

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private HealthStatus status;

    @Column(name = "yatri_pulse_user_id",length = 50)
    private UUID yatriPulseUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
    @MapsId("yatriPulseUserId")
    private YatriPulseUsers yatriPulseUsersDetails;
}
