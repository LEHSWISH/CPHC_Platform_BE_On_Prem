package org.wishfoundation.abhaservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentDocumentsRepository extends JpaRepository<ConsentDocuments, UUID> {
    @Query("SELECT c.transactionId FROM ConsentDocuments c WHERE c.hipId = ?1")
    List<UUID> findTransactionIdByHipId(String hipId);

    @Query("SELECT c.transactionId FROM ConsentDocuments c WHERE c.hipId=?1 AND c.abhaAddress = ?2")
    List<UUID> findTransactionIdByHipIdAndAbhaAddress(String hipId, String phrAddress);
}
