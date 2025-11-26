package com.taskforge.dto;

import com.taskforge.models.UserStory.Priority;
import com.taskforge.models.UserStory.Status;

import lombok.Data;

@Data
public class CreateUserStoryRequest {
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    private Long projectId;
    private String assignedToUsername;
}