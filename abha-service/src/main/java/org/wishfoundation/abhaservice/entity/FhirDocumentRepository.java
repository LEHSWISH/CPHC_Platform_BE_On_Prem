package org.wishfoundation.abhaservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FhirDocumentRepository extends JpaRepository<FhirDocuments, UUID> {
    @Query(value = "SELECT f FROM FhirDocuments f WHERE f.createdOn >= ?1 and f.createdOn <= ?2 and f.careContextId = ?3",nativeQuery = true)
    List<FhirDocuments> findByCareContextIdAndCreatedAtBetweenStartAndEnd(Instant createdOnStart, Instant createdOnEnd, String careContextId);

//    SELECT a FROM #{#entityName} a WHERE a.createdBy.yatriUserName = ?1
    @Query("select f from FhirDocuments f where f.careContextId = ?1")
    List<FhirDocuments> findByCareContextId(String careContextId);

    @Query(value = "SELECT f FROM fhir_documents f WHERE f.created_date >= :createdOnStart and f.created_date <= :createdOnEnd and f.care_context_id = :careContextId",nativeQuery = true)
    List<FhirDocuments> findByCareContextIdAndCreatedAtBetweenStartAndEndNative(@Param("createdOnStart") Instant createdOnStart,@Param("createdOnEnd") Instant createdOnEnd,@Param("careContextId") String careContextId);


}
