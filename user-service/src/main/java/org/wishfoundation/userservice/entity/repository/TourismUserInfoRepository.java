package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.wishfoundation.userservice.entity.TourismUserInfo;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link TourismUserInfo} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide basic CRUD operations.
 *
 */
public interface TourismUserInfoRepository extends JpaRepository<TourismUserInfo, UUID> {

    /**
     * Finds a {@link TourismUserInfo} entity by its idtpId.
     *
     * @param id The idtpId of the entity to find.
     * @return The found entity or null if not found.
     */
    TourismUserInfo findByIdtpId(String id);

    /**
     * Checks if a {@link TourismUserInfo} entity exists with the given idtpId.
     *
     * @param id The idtpId to check for existence.
     * @return True if an entity with the given idtpId exists, false otherwise.
     */
    boolean existsByIdtpId(String id);

    /**
     * Checks if a {@link TourismUserInfo} entity exists with the given idtpId and yatriPulseUserId.
     *
     * @param idtpId The idtpId to check for existence.
     * @param yatriPulseUserId The yatriPulseUserId to check for existence.
     * @return True if an entity with the given idtpId and yatriPulseUserId exists, false otherwise.
     */
    @Query(value = "SELECT CASE WHEN count(a)> 0 THEN true ELSE false END FROM #{#entityName} a WHERE a.idtpId = ?1 and a.yatriPulseUserId = ?2")
    boolean existsByIdtpIdAndYatriPulseUserId(String idtpId, UUID yatriPulseUserId);

    /**
     * Finds a {@link TourismUserInfo} entity by its idtpId and yatriPulseUserId.
     *
     * @param idtpId The idtpId of the entity to find.
     * @param yatriPulseUserId The yatriPulseUserId of the entity to find.
     * @return An {@link Optional} containing the found entity or an empty {@link Optional} if not found.
     */
    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.idtpId = ?1 and a.yatriPulseUserId = ?2")
    Optional<TourismUserInfo> findByIdtpIdAndUserId(String idtpId, UUID yatriPulseUserId);


}
