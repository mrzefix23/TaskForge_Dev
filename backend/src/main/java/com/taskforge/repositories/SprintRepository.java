package com.taskforge.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskforge.models.Sprint;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectId(Long projectId);
    boolean existsByNameAndProjectId(String name, Long projectId);
    Sprint findByNameAndProjectId(String name, Long projectId);
    void deleteAllByProjectId(Long projectId);
}
