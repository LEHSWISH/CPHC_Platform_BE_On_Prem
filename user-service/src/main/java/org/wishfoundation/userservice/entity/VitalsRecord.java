package org.wishfoundation.userservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vitals_record", uniqueConstraints = @UniqueConstraint(columnNames = {"consultation_id", "yatri_pulse_user_id"}))
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "yatriPulseUsers"})
public class VitalsRecord extends AuditableEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "systolic_bp", length = 10)
    private String systolicBp;

    @Column(name = "diastolic_bp", length = 10)
    private String diastolicBp;

    @Column(name = "mean_bp", length = 10)
    private String meanBp;

    @Column(name = "heart_rate", length = 10)
    private String heartRate;

    @Column(name = "spo2", length = 10)
    private String spo2;

    @Column(name = "temperature", length = 10)
    private String temperature;
    @Column(name = "temperature_units", length = 10)
    private String temperatureUnits;
    @Column(name = "temperature_source", length = 10)
    private String temperatureSource;

    @Column(name = "ecg", length = 10)
    private String ecg;

    @Column(name = "height", length = 10)
    private String height;

    @Column(name = "height_units", length = 10)
    private String heightUnits;

    @Column(name = "weight", length = 10)
    private String weight;

    @Column(name = "weight_units", length = 10)
    private String weightUnits;

    @Column(name = "blood_sugar", length = 10)
    private String bloodSugar;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "bmi", length = 10)
    private String bmi;

    @Column(name = "age", length = 10)
    private String age;

    @Column(name = "consultation_id", length = 50)
    private String consultationId;

    @Column(name = "yatri_pulse_user_id",length = 50)
    private UUID yatriPulseUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yatri_pulse_user_id", referencedColumnName = "id")
    @MapsId("yatriPulseUserId")
    @JsonBackReference("yatriPulseUser-vitalsRecord")
    private YatriPulseUsers yatriPulseUsers;
}
