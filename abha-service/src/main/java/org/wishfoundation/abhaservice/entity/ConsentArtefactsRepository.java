package org.wishfoundation.abhaservice.entity;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsentArtefactsRepository extends JpaRepository<ConsentArtefact, UUID> {
    @Query(value = "SELECT m FROM ConsentArtefact m WHERE m.transactionRequestId = ?1")
    public ConsentArtefact findByTransactionRequestId(UUID transactionRequestId);

    @Query(value = "SELECT m FROM ConsentArtefact m WHERE m.consentId = ?1")
    public List<ConsentArtefact> findByConsentId(UUID consentIds);

    @Query(value = "SELECT m FROM ConsentArtefact m WHERE m.consentId in (?1)")
    public List<ConsentArtefact> findByConsentId(List<UUID> consentIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ConsentArtefact m WHERE m.consentId = ?1")
    public void deleteAllConsentArtefactsByConsentId(UUID consentId);

}
