package org.wishfoundation.abhaservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.wishfoundation.abhaservice.enums.MedicalDocumentType;

import java.util.List;

@Repository
public interface CareContextRepository extends JpaRepository<CareContext, String> {

    @Query(value = "SELECT m FROM CareContext m WHERE m.abhaId = ?1")
    public List<CareContext> findByAbhaId(String abhaId);

    @Query(value = "SELECT m FROM CareContext m WHERE m.abhaId IS NULL and m.createdBy.yatriUserName = ?1")
    public List<CareContext> findByUserAndNullAbhaId(String userName);

    @Query("SELECT f FROM CareContext c JOIN c.fhirDocumentList f WHERE c.id in :careContextIds AND c.hiType in :hiTypes")
    List<FhirDocuments> findByCareContextIdAndHiType(@Param("careContextIds") List<String> careContextIds,@Param("hiTypes") List<MedicalDocumentType> hiTypes);

    @Query(value = "SELECT m FROM CareContext m WHERE m.abhaId IS NULL and m.id in (?1)")
    public List<CareContext> findByIdsAndNullAbhaId(List<String> ids);
}
