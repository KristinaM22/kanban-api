package com.kanban.kanban_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.kanban.kanban_api.dto.TaskDTO;
import com.kanban.kanban_api.dto.TaskInput;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
@Import(TestcontainersConfiguration.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    private TaskInput createTaskInput(String title) {
        TaskInput input = new TaskInput();
        input.setTitle(title);
        return input;
    }

    private TaskDTO createTask(String title) throws Exception {
        String response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createTaskInput(title))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return mapper.readValue(response, TaskDTO.class);
    }

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        TaskDTO dto = createTask("Created Task");
        assertEquals("Created Task", dto.getTitle());
        assertNotNull(dto.getId());
    }

    @Test
    void getTaskById_shouldReturnTask() throws Exception {
        TaskDTO dto = createTask("Returned Task");

        mockMvc.perform(get("/api/tasks/" + dto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Returned Task"));
    }

    @Test
    void updateTask_shouldUpdateFields() throws Exception {
        TaskDTO dto = createTask("Old Title");

        TaskInput update = createTaskInput("New Title");
        update.setVersion(dto.getVersion());

        mockMvc.perform(put("/api/tasks/" + dto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void patchTask_shouldPatchField() throws Exception {
        TaskDTO dto = createTask("Old Title");

        String patchJson = "{ \"title\": \"Patched Title\" }";

        mockMvc.perform(patch("/api/tasks/" + dto.getId())
                .contentType("application/merge-patch+json")
                .content(patchJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Patched Title"));
    }

    @Test
    void deleteTask_shouldRemoveTask() throws Exception {
        TaskDTO dto = createTask("Deleted Task");

        mockMvc.perform(delete("/api/tasks/" + dto.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + dto.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void listTasks_withPaginationAndFilter() throws Exception {
        mockMvc.perform(get("/api/tasks?page=0&size=10&sort=title"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void listTasks_withHateoas() throws Exception {
        for (int i = 0; i < 25; i++) createTask("Task " + i);

        mockMvc.perform(get("/api/tasks/hateoas?page=0&size=10&sort=title"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.taskDTOList").isArray())
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.first").exists())
            .andExpect(jsonPath("$._links.last").exists());
    }
}

