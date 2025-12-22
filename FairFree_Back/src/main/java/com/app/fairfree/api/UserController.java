package com.app.fairfree.api;

import com.app.fairfree.model.User;
import com.app.fairfree.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/{version}/users", version = "1", produces = "application/json")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET currently logged-in user (profile)
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(currentUser);
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User userById = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userById);
    }

}
