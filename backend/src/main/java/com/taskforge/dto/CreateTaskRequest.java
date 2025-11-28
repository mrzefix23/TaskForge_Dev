package com.taskforge.dto;

import com.taskforge.models.Task.Priority;
import com.taskforge.models.Task.Status;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private Long userStoryId;
    private String assignedToUsername;
}