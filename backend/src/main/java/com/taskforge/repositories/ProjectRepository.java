package com.taskforge.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.models.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
}