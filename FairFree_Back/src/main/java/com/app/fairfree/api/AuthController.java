package com.app.fairfree.api;

import com.app.fairfree.dto.LoginRequest;
import com.app.fairfree.dto.SignupRequest;
import com.app.fairfree.model.Role;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.RoleRepository;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already used by another account."));
        }

        User user = User.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .lastActive(LocalDateTime.now())
                .build();

        // Assign default role ROLE_USER
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        userRepository.save(user);
        logger.info("User {} successfully registered in the platform.", user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // Get roles as string
        var roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        String token = jwtService.generateToken(user.getEmail(), roles);

        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roles", roles,
                "token", token
        ));
    }

    /**
     * Map the authenticated principal (JWT) to our User.id.
     * JwtService uses the email as the subject, so Authentication.getName() is the email.
     */
    protected Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null; // or throw if feed must always be authenticated
        }

        String email = auth.getName(); // this is what you put into jwtService.generateToken(...)

        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null); // could throw if you prefer strict behaviour
    }
}

