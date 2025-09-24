package com.kanban.kanban_api.dto;

public record TaskEvent(String action, TaskDTO task) { }
