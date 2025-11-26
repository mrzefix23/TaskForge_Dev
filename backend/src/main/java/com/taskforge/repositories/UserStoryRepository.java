package com.taskforge.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.models.UserStory;

public interface UserStoryRepository extends JpaRepository<UserStory, Long> {
    List<UserStory> findByProjectId(Long projectId);
    boolean existsByTitleAndProjectId(String title, Long projectId);
    void deleteAllByProjectId(Long projectId);
}