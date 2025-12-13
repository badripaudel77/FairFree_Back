package com.app.fairfree.service;

import com.app.fairfree.dto.SignupRequest;
import com.app.fairfree.model.User;
import com.app.fairfree.model.Role;
import com.app.fairfree.dto.LoginRequest;
import com.app.fairfree.repository.RoleRepository;
import com.app.fairfree.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void verify_That_Valid_Login_Is_Fine() {
        // given the following
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        Role normalRole = new Role(1L, "ROLE_USER");
        User normalUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encodedPassword")
                .fullName("Normal User")
                .build();
        normalUser.setRoles(Set.of(normalRole));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq("user@example.com"), anySet())).thenReturn("valid-jwt-token-string");

        // when login is performed
        Map<String, Object> result = userService.loginUser(request);

        // then, assert the followings
        assertEquals("user@example.com", result.get("email"));
        assertEquals("Normal User", result.get("fullName"));
        assertEquals(Set.of("ROLE_USER"), result.get("roles"));
        assertEquals("valid-jwt-token-string", result.get("token"));

        verify(userRepository).findByEmail("user@example.com"); // default called 1 time
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtService).generateToken(eq("user@example.com"), eq(Set.of("ROLE_USER")));
    }

    @Test
    void verify_That_Invalid_Email_Login_Fails() {
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.loginUser(request));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void verify_That_Invalid_Password_Login_Fails() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        Role normalRole = new Role(1L, "ROLE_USER");
        User normalUser = User.builder()
                .email("user@example.com")
                .password("encodedPassword")
                .fullName("Normal User")
                .build();
        normalUser.setRoles(Set.of(normalRole));

//        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(normalUser));
//        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.loginUser(request));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    @Test
    void verify_That_Unique_Email_Registration_Succeeds() {
        // given
        SignupRequest request = new SignupRequest("Normal User", "user@example.com", "password123");

        Role role = Role.builder()
                .name("ROLE_USER")
                .build();
        // assume this doesn't exist in DB (first time registration)
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        // when
        userService.registerUser(request);

        // then — verify
        verify(userRepository, times(1)).findByEmail("user@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(userRepository, times(1)).save(any(User.class));

        // Ensure no extra interactions happened (only desired interactions happened).
        verifyNoMoreInteractions(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void verify_That_Duplicate_Email_Registration_Throws_Exception() {
        // given
        SignupRequest request = new SignupRequest("Normal User name", "user@example.com", "password123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

        // when called, then throws exception
        assertThrows(RuntimeException.class, () -> userService.registerUser(request));

        // Verify that only findByEmail() is called
        verify(userRepository, times(1)).findByEmail("user@example.com");

        // All other methods must NOT be called
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any());

        verifyNoMoreInteractions(userRepository, passwordEncoder, roleRepository);
    }

}
