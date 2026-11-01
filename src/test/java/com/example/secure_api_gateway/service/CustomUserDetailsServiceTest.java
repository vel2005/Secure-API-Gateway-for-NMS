package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.entity.User;
import com.example.secure_api_gateway.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testadmin");
        sampleUser.setPassword("{bcrypt}$2a$10$hashedPasswordValue");
        sampleUser.setRole("ADMIN");
        sampleUser.setEnabled(true);
    }

    @Test
    @DisplayName("Should successfully load active user and map ROLE_ADMIN authority")
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testadmin");

        assertNotNull(userDetails);
        assertEquals("testadmin", userDetails.getUsername());
        assertEquals("{bcrypt}$2a$10$hashedPasswordValue", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        boolean hasAdminRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        assertTrue(hasAdminRole, "User should have ROLE_ADMIN authority");

        verify(userRepository, times(1)).findByUsername("testadmin");
    }

    @Test
    @DisplayName("Should load disabled user with isEnabled = false")
    void testLoadUserByUsername_DisabledUser() {
        sampleUser.setEnabled(false);
        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(sampleUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testadmin");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled(), "User should be marked as disabled");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("nonexistent"));

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}

