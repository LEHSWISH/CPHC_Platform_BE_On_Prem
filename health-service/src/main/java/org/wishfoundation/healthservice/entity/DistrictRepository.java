package org.wishfoundation.healthservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for District entity.
 * Extends Spring Data JPA's JpaRepository to provide CRUD operations and custom methods.
 */
@Repository
public interface DistrictRepository extends JpaRepository<District, String> {

    /**
     * Finds all districts by the given state code.
     *
     * @param stateCode the state code to search for
     * @return a list of districts with the given state code
     */
    List<District> findByStateCode(String stateCode);

    /**
     * Finds a district by the given state code and district name.
     *
     * @param stateCode the state code to search for
     * @param districtName the district name to search for
     * @return an Optional containing the district if found, otherwise an empty Optional
     */
    Optional<District> findByStateCodeAndDistrictName(String stateCode, String districtName);

    /**
     * Checks if a district with the given district code exists.
     *
     * @param districtCode the district code to check
     * @return true if a district with the given district code exists, otherwise false
     */
    boolean existsByDistrictCode(String districtCode);

}
