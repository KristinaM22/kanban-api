package com.kanban.kanban_api.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskDTO;
import com.kanban.kanban_api.dto.TaskInput;
import com.kanban.kanban_api.mapper.TaskMapper;
import com.kanban.kanban_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskService service;
    private final ObjectMapper objectMapper;
    private final PagedResourcesAssembler<TaskDTO> pagedAssembler;

    @GetMapping
    public Page<TaskDTO> getAll(
        @RequestParam(required = false) Status status,
        @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        return service.findAll(status, pageable).map(TaskMapper::toDTO);
    }

    @GetMapping("/hateoas")
    public PagedModel<EntityModel<TaskDTO>> getAllWithHateoas(
        @RequestParam(required = false) Status status,
        @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<TaskDTO> pageDto = service.findAll(status, pageable)
            .map(TaskMapper::toDTO);

        return pagedAssembler.toModel(pageDto,
            dto -> EntityModel.of(dto,
                linkTo(methodOn(TaskController.class).getById(dto.getId())).withSelfRel()));
    }

    @GetMapping("/{id}")
    public TaskDTO getById(@PathVariable Long id) {
        return TaskMapper.toDTO(service.findByIdOrThrow(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO createTask(@Valid @RequestBody TaskInput taskInput) {
        Task created = service.create(TaskMapper.toTask(taskInput));
        return TaskMapper.toDTO(created);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskInput taskInput) {
        Task task = service.findByIdOrThrow(id);
        TaskMapper.updateTask(taskInput, task);
        Task updated = service.update(id, task);
        return TaskMapper.toDTO(updated);
    }

    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
    public TaskDTO patchTask(@PathVariable Long id, @RequestBody JsonNode patch) throws JsonProcessingException {
        Task task = service.findByIdOrThrow(id);
        Task patched = objectMapper.readerForUpdating(task).readValue(patch.toString());
        Task saved = service.patch(id, patched);
        return TaskMapper.toDTO(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        service.delete(id);
    }
}
