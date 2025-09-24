package com.kanban.kanban_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.kanban.kanban_api.dto.TaskDTO;
import com.kanban.kanban_api.mapper.TaskMapper;
import com.kanban.kanban_api.model.Priority;
import com.kanban.kanban_api.model.Status;
import com.kanban.kanban_api.model.Task;
import com.kanban.kanban_api.dto.TaskInput;
import org.junit.jupiter.api.Test;

class TaskMapperTest {

    @Test
    void toDTO_shouldMapAllFields() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("T1");
        task.setDescription("Desc");
        task.setStatus(Status.TO_DO);
        task.setPriority(Priority.HIGH);
        task.setVersion(2L);

        TaskDTO dto = TaskMapper.toDTO(task);

        assertEquals(1L, dto.getId());
        assertEquals("T1", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertEquals(Status.TO_DO, dto.getStatus());
        assertEquals(Priority.HIGH, dto.getPriority());
        assertEquals(2L, dto.getVersion());
    }

    @Test
    void toDTO_shouldReturnNull_whenInputIsNull() {
        assertNull(TaskMapper.toDTO(null));
    }

    @Test
    void toTask_shouldMapFromInput() {
        TaskInput in = new TaskInput();
        in.setTitle("X");
        in.setDescription("D");
        in.setStatus(Status.DONE);
        in.setPriority(Priority.LOW);
        in.setVersion(1L);

        Task task = TaskMapper.toTask(in);

        assertEquals("X", task.getTitle());
        assertEquals("D", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
        assertEquals(Priority.LOW, task.getPriority());
        assertEquals(1L, task.getVersion());
    }

    @Test
    void updateTask_shouldUpdateOnlyNonNullFields() {
        Task task = new Task();
        task.setTitle("Old");
        task.setDescription("OldDesc");
        task.setStatus(Status.TO_DO);

        TaskInput in = new TaskInput();
        in.setTitle("New");
        in.setPriority(Priority.HIGH);

        TaskMapper.updateTask(in, task);

        assertEquals("New", task.getTitle());
        assertEquals("OldDesc", task.getDescription());
        assertEquals(Status.TO_DO, task.getStatus());
        assertEquals(Priority.HIGH, task.getPriority());
    }
}

