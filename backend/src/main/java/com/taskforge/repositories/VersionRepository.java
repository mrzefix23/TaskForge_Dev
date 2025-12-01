package com.taskforge.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskforge.models.Version;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {
    
    List<Version> findByProjectIdOrderByIdDesc(Long projectId);
    
    Optional<Version> findByProjectIdAndVersionNumber(Long projectId, String versionNumber);
    
    boolean existsByProjectIdAndVersionNumber(Long projectId, String versionNumber);
}