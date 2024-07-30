package org.wishfoundation.userservice.entity.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.wishfoundation.userservice.entity.DocumentsPath;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentsPathRepository extends JpaRepository<DocumentsPath, UUID> {
    @Modifying
    @Transactional
    @Query("DELETE FROM #{#entityName} where fileName = ?1 and filePath =?2 and yatriPulseUserId = ?3")
    void deleteByFileNameAndUserId(String fileName, String filePath , UUID userId);

    @Query(value = "SELECT m FROM #{#entityName} m WHERE m.yatriPulseUserId = ?1")
    List<DocumentsPath> findByUserId(UUID userId);
}
