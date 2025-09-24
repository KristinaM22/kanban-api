package com.kanban.kanban_api.controller;

import com.kanban.kanban_api.model.Priority;
import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskDTO;
import com.kanban.kanban_api.dto.TaskInput;
import com.kanban.kanban_api.mapper.TaskMapper;
import com.kanban.kanban_api.service.TaskService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TaskGraphQLController {

    private final TaskService taskService;

    public TaskGraphQLController(TaskService taskService) {
        this.taskService = taskService;
    }

    @QueryMapping
    public TaskDTO task(@Argument Long id) {
        return TaskMapper.toDTO(taskService.findByIdOrThrow(id));
    }

    @QueryMapping
    public List<TaskDTO> tasks(@Argument Status status,
        @Argument Integer page,
        @Argument Integer size) {
        Pageable pageable = PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 20
        );
        return taskService.findAll(status, pageable)
            .stream()
            .map(TaskMapper::toDTO)
            .toList();
    }

    @MutationMapping
    public TaskDTO createTask(@Argument String title,
        @Argument String description,
        @Argument Status status,
        @Argument Priority priority) {
        TaskInput input = new TaskInput();
        input.setTitle(title);
        input.setDescription(description);
        input.setStatus(status);
        input.setPriority(priority);
        Task task = taskService.create(TaskMapper.toTask(input));
        return TaskMapper.toDTO(task);
    }

    @MutationMapping
    public TaskDTO updateTask(@Argument Long id,
        @Argument String title,
        @Argument String description,
        @Argument Status status,
        @Argument Priority priority,
        @Argument Long version) {
        TaskInput input = new TaskInput();
        input.setTitle(title);
        input.setDescription(description);
        input.setStatus(status);
        input.setPriority(priority);
        input.setVersion(version);
        Task task = taskService.update(id, TaskMapper.toTask(input));
        return TaskMapper.toDTO(task);
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        taskService.delete(id);
        return true;
    }
}

