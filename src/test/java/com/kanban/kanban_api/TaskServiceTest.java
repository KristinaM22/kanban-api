package com.kanban.kanban_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskEvent;
import com.kanban.kanban_api.repository.TaskRepository;
import com.kanban.kanban_api.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repository;
    @Mock
    SimpMessagingTemplate messaging;
    @InjectMocks
    TaskService service;

    @Test
    void create_shouldSaveTask() {
        Task task = new Task();
        task.setTitle("Test");

        when(repository.save(any(Task.class))).thenReturn(task);

        Task saved = service.create(task);

        assertEquals("Test", saved.getTitle());
        verify(repository).save(task);
    }

    @Test
    void update_shouldSaveTask() {
        Task old = new Task();
        old.setId(1L);
        old.setTitle("Old");

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task newTask = new Task();
        newTask.setTitle("New");

        Task saved = service.update(1L, newTask);

        assertEquals("New", saved.getTitle());
        verify(repository).save(any(Task.class));
    }

    @Test
    void patch_shouldSaveTask() {
        Task old = new Task();
        old.setId(1L);
        old.setTitle("Old");

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Task patchedTask = new Task();
        patchedTask.setTitle("Patched");

        Task saved = service.patch(1L, patchedTask);

        assertEquals("Patched", saved.getTitle());
        verify(repository).save(any(Task.class));
    }

    @Test
    void delete_shouldRemoveTask() {
        Task existing = new Task();
        existing.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
    }

    @Test
    void findByIdOrThrow_shouldThrowWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.findByIdOrThrow(1L));
    }

    @Test
    void findAll_shouldCallRepository_withStatus() {
        Task t = new Task();
        Page<Task> page = new PageImpl<>(List.of(t));
        when(repository.findByStatus(any(), any(PageRequest.class))).thenReturn(page);

        Page<Task> result = service.findAll(Status.TO_DO, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(repository).findByStatus(eq(Status.TO_DO), any(PageRequest.class));
        verify(repository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void findAll_shouldCallRepository_whenStatusNull() {
        Task t = new Task();
        Page<Task> page = new PageImpl<>(List.of(t));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Task> result = service.findAll(null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        verify(repository).findAll(any(PageRequest.class));
    }

    // broadcast
    @Test
    void create_shouldBroadcastTaskEvent() {
        Task task = new Task();
        task.setTitle("Test");

        when(repository.save(any(Task.class))).thenReturn(task);

        service.create(task);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(messaging).convertAndSend(eq("/topic/tasks"), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();
        assertEquals("CREATED", event.action());
        assertEquals("Test", event.task().getTitle());
    }

    @Test
    void update_shouldBroadcastTaskEvent() {
        Task old = new Task();
        old.setId(1L);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Task newTask = new Task();
        newTask.setTitle("New");

        service.update(1L, newTask);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(messaging).convertAndSend(eq("/topic/tasks"), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();
        assertEquals("UPDATED", event.action());
        assertEquals("New", event.task().getTitle());
    }

    @Test
    void patch_shouldBroadcastTaskEvent() {
        Task old = new Task();
        old.setId(1L);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Task patchedTask = new Task();
        patchedTask.setTitle("Patched");

        service.patch(1L, patchedTask);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(messaging).convertAndSend(eq("/topic/tasks"), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();
        assertEquals("UPDATED", event.action());
        assertEquals("Patched", event.task().getTitle());
    }

    @Test
    void delete_shouldBroadcastTaskEvent() {
        Task existing = new Task();
        existing.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(messaging).convertAndSend(eq("/topic/tasks"), eventCaptor.capture());

        TaskEvent event = eventCaptor.getValue();
        assertEquals("DELETED", event.action());
        assertEquals(existing.getId(), event.task().getId());
    }
}

