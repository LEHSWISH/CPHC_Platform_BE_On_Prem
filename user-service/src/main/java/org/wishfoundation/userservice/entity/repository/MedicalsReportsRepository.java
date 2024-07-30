package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.MedicalsReports;

import java.util.UUID;

@Repository
public interface MedicalsReportsRepository extends JpaRepository<MedicalsReports, UUID> {
    @Query(value = "SELECT m FROM #{#entityName} m WHERE m.yatriPulseUserId = ?1")
    MedicalsReports findMedicalReportsByUserId(UUID userId);
}
