package com.app.fairfree.service;

import com.app.fairfree.dto.LoginRequest;
import com.app.fairfree.dto.SignupRequest;
import com.app.fairfree.exception.BadRequestException;
import com.app.fairfree.exception.ResourceNotFoundException;
import com.app.fairfree.model.Role;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.RoleRepository;
import com.app.fairfree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    static Logger logger = LoggerFactory.getLogger(UserService.class);

    public Map<String, Object> loginUser(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User with given details not found."));
        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid Password");
        }
        // Convert roles to set of string names
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        // Generate JWT
        String token = jwtService.generateToken(user.getEmail(), roles);

        // Response map
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roles", roles,
                "token", token
        );
    }

    public void registerUser(SignupRequest request) {
        // Check duplicate email
        userRepository.findByEmail(request.email())
                .ifPresent(u -> {
                    throw new BadRequestException("Email already used by another account.");
                });
        // Create user
        User user = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .lastActive(LocalDateTime.now())
                .build();

        // Set default role
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        user.setRoles(Set.of(defaultRole));
        // Save user
        userRepository.save(user);
        logger.info("User {} successfully registered.", user.getEmail());
    }

    // Get user by Id
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Get Currently logged in User
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
