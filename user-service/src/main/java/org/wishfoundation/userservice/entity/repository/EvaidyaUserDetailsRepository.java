package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.EvaidyaUserDetails;
import org.wishfoundation.userservice.enums.HealthStatus;

import java.util.UUID;

@Repository
public interface EvaidyaUserDetailsRepository extends JpaRepository<EvaidyaUserDetails, UUID> {
    @Query(value = "SELECT CASE WHEN count(a)> 0 THEN true ELSE false END FROM #{#entityName} a WHERE a.evaidyaUserId = ?1")
    boolean existsByEvaidyaUserId(String evaidyaUserId);

    @Query(value = "SELECT a.status FROM #{#entityName} a WHERE a.id = ?1")
    HealthStatus findStatusById(UUID id);
}
