package org.wishfoundation.chardhamcore.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.chardhamcore.entity.YatriPulseUsers;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface YatriPulseUsersRepository extends JpaRepository<YatriPulseUsers, UUID> {
    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.userName = ?1")
    Optional<YatriPulseUsers> findUserByUserName(String userName);
}
