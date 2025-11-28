package com.taskforge.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskforge.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserStoryId(Long userStoryId);
    boolean existsByTitleAndUserStoryId(String title, Long userStoryId);
    Task findByTitleAndUserStoryId(String title, Long userStoryId);
    void deleteAllByUserStoryId(Long userStoryId);
}