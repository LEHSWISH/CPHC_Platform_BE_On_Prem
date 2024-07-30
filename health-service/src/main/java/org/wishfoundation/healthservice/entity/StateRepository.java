package org.wishfoundation.healthservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * This interface represents a repository for managing {@link State} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations
 * and additional methods for custom queries.
 *
 *
 * @since 1.0.0
 */
@Repository
public interface StateRepository extends JpaRepository<State, String>{

    /**
     * Checks if a state with the given state code exists in the database.
     *
     * @param stateCode the state code to check
     * @return true if a state with the given state code exists, false otherwise
     */
    boolean existsByStateCode(String stateCode);

    /**
     * Finds a state by its name.
     *
     * @param name the name of the state to find
     * @return an {@link Optional} containing the found state, or an empty {@link Optional} if no state is found
     */
    Optional<State> findByStateName(String name);
}
