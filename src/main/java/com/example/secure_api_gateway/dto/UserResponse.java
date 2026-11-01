package com.example.secure_api_gateway.dto;

public record UserResponse(
        Long id,
        String username,
        String role) {
}