package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.config.Role;
import com.example.secure_api_gateway.entity.User;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("plainPass");
        sampleUser.setRole(Role.VIEWER);
        sampleUser.setEnabled(true);
    }

    @Test
    @DisplayName("Create user - successfully assigns VIEWER role and hashes password")
    void testCreateUser_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("hashedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = userService.createUser(sampleUser);

        assertNotNull(created);
        assertEquals(Role.VIEWER, created.getRole());
        assertEquals("hashedPass", created.getPassword());
        assertTrue(created.isEnabled());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Update user role - successfully promotes VIEWER to ENGINEER")
    void testUpdateUserRole_ToEngineer_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserRole(10L, Role.ENGINEER);

        assertNotNull(updated);
        assertEquals(Role.ENGINEER, updated.getRole());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Update user role - successfully promotes VIEWER to ADMIN")
    void testUpdateUserRole_ToAdmin_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserRole(10L, Role.ADMIN);

        assertNotNull(updated);
        assertEquals(Role.ADMIN, updated.getRole());
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    @DisplayName("Update user role - throws ResourceNotFoundException when user does not exist")
    void testUpdateUserRole_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUserRole(999L, Role.ENGINEER));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update user role - throws IllegalArgumentException for invalid role name")
    void testUpdateUserRole_InvalidRole() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.updateUserRole(10L, "SUPERUSER"));

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).save(any());
    }
}

