package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wishfoundation.userservice.entity.CareGiverCareRecipient;
import org.wishfoundation.userservice.enums.RequestStatus;
import org.wishfoundation.userservice.response.ChildUserIdStatusResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CareGiverCareRecipientRepository extends JpaRepository<CareGiverCareRecipient, UUID> {
    static final String CHILD_USER_ID_STATUS_RESPONSE = "new org.wishfoundation.userservice.response.ChildUserIdStatusResponse";

    @Query(value = "SELECT COUNT(a) > 0 FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.childYatriPulseUserId = ?2")
    boolean existsByParentAndChildUserId(UUID parentUserId, UUID childUserId);

    @Query(value = "SELECT COUNT(a) > 0 FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.requestStatus = ?2")
    boolean existsByUserIdAndRequestStatus(UUID userId, RequestStatus requestStatus);

    @Query(value = "SELECT a.requestStatus FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.childYatriPulseUserId = ?2")
    RequestStatus findRequestStatusByParentAndChildUserId(UUID parentUserId, UUID childUserId);

    @Query(value = "SELECT a.childYatriPulseUserId FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.requestStatus = ?2")
    List<UUID> findChildUserIdByParentUserId(UUID parentYatriPulseUserId, RequestStatus requestStatus);

    @Query(value = "SELECT " + CHILD_USER_ID_STATUS_RESPONSE + " (a.childYatriPulseUserId, a.requestStatus) FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.requestStatus = ?2")
    List<ChildUserIdStatusResponse> findChildIdAndRequestStatusByParentUserId(UUID parentYatriPulseUserId, RequestStatus requestStatus);

    @Transactional
    @Modifying
    @Query("update #{#entityName} y set y.requestStatus = ?1, y.requestAcceptTime = ?2 where y.parentYatriPulseUserId = ?3 and y.childYatriPulseUserId = ?4")
    void updateRequestStatus(RequestStatus requestStatus, Instant currentTime, UUID parentYatriPulseUserId, UUID childYatriPulseUserId);

    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.childYatriPulseUserId = ?2")
    void deleteByParentAndChildUserId(UUID parentUserId, UUID childUserId);

    @Query("SELECT COUNT(a) FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.childYatriPulseUserId = ?2 AND a.requestStatus = ?3")
    int countByParentAndChildUserIdAndRequestStatus(UUID parentUserId, UUID childUserId, RequestStatus requestStatus);

    @Query(value = "SELECT a.parentYatriPulseUserId FROM #{#entityName} a WHERE a.childYatriPulseUserId = ?1 and a.requestStatus = ?2")
    UUID findParentUserIdByChildAndRequestStatus(UUID childYatriPulseUserId, RequestStatus requestStatus);

    @Query(value = "SELECT COUNT(a) > 0 FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.childYatriPulseUserId = ?2 and a.requestStatus = ?3")
    boolean existsByParentAndChildUserIdAndRequestStatus(UUID parentUserId, UUID childUserId, RequestStatus requestStatus);
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} a WHERE a.childYatriPulseUserId = ?1 AND a.requestStatus = ?2")
    void deleteByChildUserIdAndRequestStatus(UUID userId, RequestStatus requestStatus);

    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1 AND a.requestStatus = ?2")
    void deleteByParentUserIdAndRequestStatus(UUID userId, RequestStatus requestStatus);


    @Query("SELECT COUNT(a) FROM #{#entityName} a WHERE a.parentYatriPulseUserId = ?1")
    int countByParentUserId(UUID parentUserId);
}
