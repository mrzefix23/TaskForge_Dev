package com.taskforge.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
    private UserDto user;
    private List<UserDto> members;
}