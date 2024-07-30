package org.wishfoundation.superadmin.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.superadmin.entity.UserAccounts;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountsRepository extends JpaRepository<UserAccounts, UUID> {

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.email = ?1")
    Optional<UserAccounts> findUserByEmail(String email);

    boolean existsByEmail(String emailId);


}
