package com.example.secure_api_gateway.controller;

import com.example.secure_api_gateway.dto.RoleUpdateRequest;
import com.example.secure_api_gateway.dto.UserResponse;
import com.example.secure_api_gateway.entity.User;
import com.example.secure_api_gateway.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Public registration endpoint (accessible at both /api/users and /api/users/register)
    @PostMapping(path = {"", "/register"})
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody User user) {

        User savedUser = userService.createUser(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole());
    }

    // Restricted to ADMIN
    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRole()))
                .toList();
    }

    // Restricted to ADMIN via SecurityConfig: .requestMatchers("/api/users/**").hasRole("ADMIN")
    @PutMapping("/{id}/role")
    public UserResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        User updatedUser = userService.updateUserRole(id, request.role());

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getRole());
    }
}
