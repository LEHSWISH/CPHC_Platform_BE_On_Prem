package org.wishfoundation.userservice.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.YatriDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface YatriDetailsRepository extends JpaRepository<YatriDetails, UUID> {

    @Query(value = "SELECT a FROM YatriDetails a WHERE a.createdBy.auditUserName in (?1)")
    ArrayList<YatriDetails> findByCreatedByUserNames(List<String> userName);

}
