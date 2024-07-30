package org.wishfoundation.healthservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This interface represents a repository for managing {@link HealthFacility} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations
 * and additional methods for custom queries.
 *
 *
 * @since 1.0.0
 */
@Repository
public interface HealthFacilityRepository extends JpaRepository<HealthFacility, String>{
    /**
     * Finds all {@link HealthFacility} entities that match the given state code and district code.
     *
     * @param stateCode the state code to match
     * @param districtCode the district code to match
     * @return a list of {@link HealthFacility} entities that match the given state code and district code
     */
     List<HealthFacility> findByStateCodeAndDistrictCode(String stateCode, String districtCode);

}
