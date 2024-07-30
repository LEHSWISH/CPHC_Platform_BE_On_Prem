package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.enums.CareType;
import org.wishfoundation.userservice.enums.CreationType;
import org.wishfoundation.userservice.enums.GovernmentIdType;
import org.wishfoundation.userservice.response.BulkUserDetailsResponse;
import org.wishfoundation.userservice.response.CareGiverCareRecipientResponse;
import org.wishfoundation.userservice.response.UserAuthDetailsResp;
import org.wishfoundation.userservice.response.UserBasicDetailsResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface YatriPulseUsersRepository extends JpaRepository<YatriPulseUsers, UUID> {

    String USER_BASIC_DETAILS_RESP = "new org.wishfoundation.userservice.response.UserBasicDetailsResp";
    String USER_AUTH_DETAILS_RESP = "new org.wishfoundation.userservice.response.UserAuthDetailsResp";
    String BULK_USER_DETAILS_RESP = "new org.wishfoundation.userservice.response.BulkUserDetailsResponse";
    String CARE_GIVER_CARE_TAKER_RESP = "new org.wishfoundation.userservice.response.CareGiverCareRecipientResponse";

    @Transactional
    @Modifying
    @Query("update #{#entityName} y set y.tourismId = ?1 where y.userName = ?2")
    void updateTourismIdByUserName(UUID tourismId, String userName);

    @Query(value = "SELECT CASE WHEN count(a)> 0 THEN true ELSE false END FROM #{#entityName} a WHERE a.userName = ?1 and a.phoneNumber = ?2")
    boolean existsByUserNameAndPhoneNumber(String userName, String phoneNumber);

    @Query(value = "SELECT CASE WHEN count(a)> 0 THEN true ELSE false END FROM #{#entityName} a WHERE a.userName = ?1")
    boolean existsByUserName(String userName);

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.userName = ?1 and a.phoneNumber = ?2")
    YatriPulseUsers findByUserNameAndPhoneNumber(String userName, String phoneNumber);

    @Query(value = "SELECT a.userName FROM #{#entityName} a WHERE a.phoneNumber = ?1")
    ArrayList<String> findUsersByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.userName = ?1")
    Optional<YatriPulseUsers> findUserByUserName(String userName);

    @Query(value = "SELECT a.abhaUserId FROM #{#entityName} a WHERE a.phoneNumber = ?1")
    ArrayList<UUID> findAllUserIdByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT a.userName FROM #{#entityName} a WHERE a.abhaUserId = ?1")
    String findUserByAbhaUserId(UUID abhaUserId);

    @Query(value = "SELECT a.userName FROM #{#entityName} a WHERE a.phoneNumber = ?1 and a.governmentIdType = ?2 and a.governmentId = ?3")
    String findUserByPhoneNumberAndGovIdTypeAndGovId(String phoneNumber, GovernmentIdType governmentIdType, String governmentId);

    @Query(value = "SELECT " + USER_AUTH_DETAILS_RESP + " (a.id, a.userName, a.password, a.phoneNumber, a.careType) FROM #{#entityName} a WHERE a.userName = ?1")
    Optional<UserAuthDetailsResp> findUserAuthDetailsByUserName(String userName);

    @Query(value = "SELECT CASE WHEN count(a)> 0 THEN true ELSE false END FROM #{#entityName} a WHERE a.governmentId = ?1")
    boolean existsByGovernmentId(String governmentId);

    @Query(value = "SELECT " + CARE_GIVER_CARE_TAKER_RESP + " (a.id, a.userName, a.phoneNumber) FROM #{#entityName} a WHERE a.id in (?1)")
    List<CareGiverCareRecipientResponse> findUserNameByIds(List<UUID> userIds);

    @Query(value = "SELECT " + USER_BASIC_DETAILS_RESP + " (yp.id, yp.userName, yp.phoneNumber, yd.fullName, ed.status) FROM YatriPulseUsers yp INNER JOIN YatriDetails yd ON yp.id = yd.yatriPulseUserId FULL JOIN EvaidyaUserDetails ed on yp.id  = ed.yatriPulseUserId WHERE yp.id in (?1)")
    List<UserBasicDetailsResp> findBasicDetailsByIds(List<UUID> userIds);
    @Transactional
    @Modifying
    @Query("update #{#entityName} y set y.careType = ?1 where y.id = ?2")
    void updateCareType(CareType careType, UUID requestedUserId);

    @Query(value = "SELECT " + CARE_GIVER_CARE_TAKER_RESP + " (a.userName, yd.fullName) FROM YatriPulseUsers a FULL JOIN YatriDetails yd  on a.id = yd.yatriPulseUserId WHERE a.phoneNumber = ?1")
    List<CareGiverCareRecipientResponse> findUserNameAndFullNameByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT " + BULK_USER_DETAILS_RESP + "(yd.fullName, yp.userName, yp.phoneNumber, tu.idtpId, yd.gender, yd.address, yd.state, yd.district) FROM YatriPulseUsers yp INNER JOIN YatriDetails yd ON yp.id = yd.yatriPulseUserId FULL JOIN TourismUserInfo tu on yp.id  = tu.yatriPulseUserId WHERE yp.creationType =?1")
    List<BulkUserDetailsResponse> findUserDetailsbyCreationType(CreationType creationType);

}
