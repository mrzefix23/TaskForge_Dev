package com.taskforge.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.taskforge.models.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);

    @Query("SELECT p FROM Project p WHERE p.owner.username = :username OR :username IN (SELECT m.username FROM p.members m)")
    List<Project> findAllByOwnerOrMember(String username);
}