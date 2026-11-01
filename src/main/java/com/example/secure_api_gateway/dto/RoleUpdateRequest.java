package com.example.secure_api_gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleUpdateRequest(
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "^(ADMIN|ENGINEER|VIEWER)$", message = "Role must be ADMIN, ENGINEER, or VIEWER")
        String role
) {}

