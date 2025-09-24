package com.kanban.kanban_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfiguration.class)
class TaskGraphQLTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private String performGraphQL(String queryOrMutation) throws Exception {
        String json = mapper.writeValueAsString(Map.of("query", queryOrMutation));
        return mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Test
    void shouldCreateTask() throws Exception {
        String mutation = """
            mutation {
              createTask(title: "Test Task", description: "demo", status: TO_DO, priority: LOW) {
                id
                title
                status
              }
            }
        """;

        String json = performGraphQL(mutation);

        assertEquals("Test Task", JsonPath.read(json, "$.data.createTask.title"));
        assertEquals("TO_DO", JsonPath.read(json, "$.data.createTask.status"));
    }

    @Test
    void shouldFetchTaskById() throws Exception {
        String createMutation = """
            mutation {
              createTask(title: "Fetch Me", description: "demo", status: TO_DO, priority: LOW) {
                id
              }
            }
        """;

        String jsonCreate = performGraphQL(createMutation);
        String taskId = JsonPath.read(jsonCreate, "$.data.createTask.id").toString();

        String query = """
            query {
              task(id: %s) {
                id
                title
              }
            }
        """.formatted(taskId);

        String jsonQuery = performGraphQL(query);

        assertEquals("Fetch Me", JsonPath.read(jsonQuery, "$.data.task.title"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        String createMutation = """
            mutation {
              createTask(title: "Old Title", description: "demo", status: TO_DO, priority: LOW) {
                id
                version
              }
            }
        """;

        String jsonCreate = performGraphQL(createMutation);
        String id = JsonPath.read(jsonCreate, "$.data.createTask.id").toString();
        String version = JsonPath.read(jsonCreate, "$.data.createTask.version").toString();

        String updateMutation = """
            mutation {
              updateTask(id: %s, title: "Updated Title", description: "demo2", status: IN_PROGRESS, priority: HIGH, version: %s) {
                id
                title
                status
                priority
              }
            }
        """.formatted(id, version);

        String jsonUpdate = performGraphQL(updateMutation);

        assertEquals("Updated Title", JsonPath.read(jsonUpdate, "$.data.updateTask.title"));
        assertEquals("IN_PROGRESS", JsonPath.read(jsonUpdate, "$.data.updateTask.status"));
        assertEquals("HIGH", JsonPath.read(jsonUpdate, "$.data.updateTask.priority"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        String createMutation = """
            mutation {
              createTask(title: "Delete Me", description: "demo", status: TO_DO, priority: LOW) {
                id
              }
            }
        """;

        String jsonCreate = performGraphQL(createMutation);
        String taskId = JsonPath.read(jsonCreate, "$.data.createTask.id").toString();

        String deleteMutation = """
            mutation {
              deleteTask(id: %s)
            }
        """.formatted(taskId);

        String jsonDelete = performGraphQL(deleteMutation);

        boolean deleted = JsonPath.read(jsonDelete, "$.data.deleteTask");
        assertTrue(deleted);
    }

    @Test
    void shouldListTasks() throws Exception {
        for (int i = 0; i < 2; i++) {
            String createMutation = """
                mutation {
                  createTask(title: "List Task %d", description: "demo", status: TO_DO, priority: LOW) {
                    id
                  }
                }
            """.formatted(i);

            performGraphQL(createMutation);
        }

        String query = """
            query {
              tasks(page: 0, size: 10) {
                id
                title
              }
            }
        """;

        String jsonQuery = performGraphQL(query);
        int size = JsonPath.read(jsonQuery, "$.data.tasks.length()");

        assertTrue(size >= 2);
    }

    @Test
    void shouldListTasksWithDefaultPageAndSize() throws Exception {
        for (int i = 0; i < 3; i++) {
            String createMutation = """
            mutation {
              createTask(title: "Default Paging %d", description: "demo", status: TO_DO, priority: LOW) {
                id
              }
            }
        """.formatted(i);

            performGraphQL(createMutation);
        }

        String query = """
        query {
          tasks {
            id
            title
          }
        }
    """;

        String jsonQuery = performGraphQL(query);
        int size = JsonPath.read(jsonQuery, "$.data.tasks.length()");

        assertTrue(size >= 3);
    }


}