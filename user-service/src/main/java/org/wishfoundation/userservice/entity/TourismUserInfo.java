package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.wishfoundation.userservice.enums.Gender;

import java.io.Serial;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "tourism_user_info")
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsers"})
@ToString
public class TourismUserInfo extends  AuditableEntity{

    @Serial
    private static final long serialVersionUID = -4630428315685866126L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name= "idtp_id",unique = true)
    private String idtpId;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "gender",length=15)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "age",length = 5)
    private int age;

    @Column(name = "email_id", length = 100)
    private String emailId;

    @Column(name = "tour_start_date", length = 20)
    private String tourStartDate;

    @Column(name = "tour_end_date", length = 20)
    private String tourEndDate;

    @Column(name = "tour_duration", length = 5)
    private int tourDuration;

    @Column(name = "address", length = 160)
    private String address;

    //TODO : will modify char length once we know the exact length value
    @Column(name = "disease")
    private String disease;

    @Column(name = "other_disease")
    private String otherDisease;

    @Column(name = "passenger_id",length = 10)
    private int passengerId;

    @Column(name="full_name",length= 50)
    private String fullName;

    @Column(name="yatra_id",length= 50)
    private long yatraId;

    @Column(name = "yatri_pulse_user_id", length = 50)
    private UUID yatriPulseUserId;

    @JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("yatriPulseUserId")
    private YatriPulseUsers yatriPulseUsers;


}
