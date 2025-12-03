package com.taskforge.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskforge.models.KanbanColumn;

@Repository
public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {
    List<KanbanColumn> findByProjectIdOrderByOrderAsc(Long projectId);
    Optional<KanbanColumn> findByStatusAndProjectId(String status, Long projectId);
    boolean existsByStatusAndProjectId(String status, Long projectId);
    void deleteAllByProjectId(Long projectId);
}
