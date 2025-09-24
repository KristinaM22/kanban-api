package com.kanban.kanban_api.service;

import com.kanban.kanban_api.mapper.TaskMapper;
import com.kanban.kanban_api.repository.TaskRepository;
import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final SimpMessagingTemplate messaging;

    @Cacheable(value = "tasksList", key = "{#status, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<Task> findAll(Status status, Pageable pageable) {
        if (status != null) {
            return repository.findByStatus(status, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    @Cacheable(value = "tasks", key = "#id")
    public Task findByIdOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
    }

    @CacheEvict(value = {"tasks", "tasksList"}, allEntries = true)
    public Task create(Task task) {
        Task saved = repository.save(task);
        messaging.convertAndSend("/topic/tasks", new TaskEvent("CREATED", TaskMapper.toDTO(saved)));
        return saved;
    }

    @CacheEvict(value = {"tasks", "tasksList"}, allEntries = true)
    public Task update(Long id, Task task) {
        Task saved = repository.save(task);
        messaging.convertAndSend("/topic/tasks", new TaskEvent("UPDATED", TaskMapper.toDTO(saved)));
        return saved;
    }

    @CacheEvict(value = {"tasks", "tasksList"}, allEntries = true)
    public Task patch(Long id, Task task) {
        Task saved = repository.save(task);
        messaging.convertAndSend("/topic/tasks", new TaskEvent("UPDATED", TaskMapper.toDTO(saved)));
        return saved;
    }

    @CacheEvict(value = {"tasks", "tasksList"}, allEntries = true)
    public void delete(Long id) {
        Task task = findByIdOrThrow(id);
        repository.delete(task);
        messaging.convertAndSend("/topic/tasks", new TaskEvent("DELETED", TaskMapper.toDTO(task)));
    }
}
