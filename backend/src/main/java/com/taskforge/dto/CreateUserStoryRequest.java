package com.taskforge.dto;

import com.taskforge.models.UserStory.Priority;

import lombok.Data;
import java.util.List;

@Data
public class CreateUserStoryRequest {
    private String title;
    private String description;
    private Priority priority;
    private String status;
    private Long projectId;
    private List<String> assignedToUsernames;
    private Long sprintId;
}