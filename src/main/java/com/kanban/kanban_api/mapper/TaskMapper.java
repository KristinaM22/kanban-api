package com.kanban.kanban_api.mapper;

import com.kanban.kanban_api.dto.TaskDTO;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskInput;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskMapper {

    public static TaskDTO toDTO(Task t) {
        if (t == null) return null;
        TaskDTO dto = new TaskDTO();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        dto.setStatus(t.getStatus());
        dto.setPriority(t.getPriority());
        dto.setVersion(t.getVersion());
        return dto;
    }

    public static Task toTask(TaskInput in) {
        Task t = new Task();
        t.setTitle(in.getTitle());
        t.setDescription(in.getDescription());
        Optional.ofNullable(in.getStatus()).ifPresent(t::setStatus);
        Optional.ofNullable(in.getPriority()).ifPresent(t::setPriority);
        Optional.ofNullable(in.getVersion()).ifPresent(t::setVersion);
        return t;
    }

    public static void updateTask(TaskInput in, Task t) {
        Optional.ofNullable(in.getTitle()).ifPresent(t::setTitle);
        Optional.ofNullable(in.getDescription()).ifPresent(t::setDescription);
        Optional.ofNullable(in.getStatus()).ifPresent(t::setStatus);
        Optional.ofNullable(in.getPriority()).ifPresent(t::setPriority);
        Optional.ofNullable(in.getVersion()).ifPresent(t::setVersion);
    }
}
