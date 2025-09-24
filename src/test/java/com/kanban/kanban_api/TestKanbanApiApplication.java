package com.kanban.kanban_api;

import org.springframework.boot.SpringApplication;

public class TestKanbanApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(KanbanApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
