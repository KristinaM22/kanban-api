package com.kanban.kanban_api.dto;

import com.kanban.kanban_api.validator.EnumValidator;
import com.kanban.kanban_api.model.Priority;
import com.kanban.kanban_api.model.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskInput {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @EnumValidator(enumClass = Status.class, message = "Invalid status")
    private Status status;

    @EnumValidator(enumClass = Priority.class, message = "Invalid priority")
    private Priority priority;

    private Long version;
}
