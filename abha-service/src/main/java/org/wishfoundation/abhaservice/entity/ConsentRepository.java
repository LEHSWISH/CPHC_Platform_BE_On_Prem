package org.wishfoundation.abhaservice.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface ConsentRepository extends JpaRepository<Consent, UUID> {

    @Query(value = "SELECT a FROM #{#entityName} a WHERE a.createdBy.yatriUserName = ?1")
    public List<Consent> findByCreatedBy(String userName);
}
