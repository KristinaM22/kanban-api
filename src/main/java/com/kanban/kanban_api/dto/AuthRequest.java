package com.kanban.kanban_api.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
