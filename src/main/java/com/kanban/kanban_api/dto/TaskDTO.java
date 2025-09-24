package com.kanban.kanban_api.dto;

import com.kanban.kanban_api.model.Priority;
import com.kanban.kanban_api.model.Status;
import lombok.Data;

@Data
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Long version;
}
