package com.taskforge.dto;

import lombok.Data;

@Data
public class CreateKanbanColumnRequest {
    private String name;
    private String status;
    private Integer order;
    private Long projectId;
}
