package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.VitalsRecord;
import org.wishfoundation.userservice.response.VitalsRecordResponse;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VitalsRecordRepository extends JpaRepository<VitalsRecord, UUID> {
    String VITALS_RESPONSE = "new org.wishfoundation.userservice.response.VitalsRecordResponse";

    @Query(value = "SELECT " + VITALS_RESPONSE + " (vr.systolicBp, vr.diastolicBp, vr.meanBp, vr.heartRate, vr.spo2, vr.temperature, vr.temperatureUnits, vr.temperatureSource, vr.ecg, vr.height, vr.heightUnits, vr.weight, vr.weightUnits, vr.bloodSugar, vr.location, vr.bmi, vr.age, vr.consultationId, e.status, yd.fullName) " +
            "FROM VitalsRecord vr " +
            "INNER JOIN YatriDetails yd ON vr.yatriPulseUserId = yd.yatriPulseUserId " +
            "INNER JOIN EvaidyaUserDetails e ON vr.yatriPulseUserId = e.yatriPulseUserId " +
            "WHERE vr.yatriPulseUserId = ?1 order by vr.updatedOn desc limit 1")
    Optional<VitalsRecordResponse> fetchVitalsResponse(UUID yatriPulseUserId);

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.yatriPulseUserId = ?1")
    VitalsRecord FindByYatriPulseUserId(UUID userId);

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.consultationId = ?1 and a.yatriPulseUserId = ?2")
    Optional<VitalsRecord> findByConsultationId(String consultationId, UUID userId);

}
