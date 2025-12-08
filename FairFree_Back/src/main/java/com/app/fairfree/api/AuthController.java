package com.app.fairfree.api;

import com.app.fairfree.dto.LoginRequest;
import com.app.fairfree.dto.SignupRequest;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        Map<String, Object> response = userService.loginUser(request);
        return ResponseEntity.ok(response);
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

