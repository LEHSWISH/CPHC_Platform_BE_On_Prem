package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.ABHAUserDetails;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ABHAUserDetailsRepository extends JpaRepository<ABHAUserDetails, UUID> {
   @Query(value = "SELECT a FROM #{#entityName} a WHERE a.abhaNumber = ?1")
   Optional<ABHAUserDetails> findByAbhaNumber(String abhaNumber);
   @Query(value = "SELECT a FROM #{#entityName} a WHERE a.id in ?1")
   ArrayList<ABHAUserDetails> findByUserIds(ArrayList<UUID> abhaUserIds);

}
