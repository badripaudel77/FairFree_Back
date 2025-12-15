package com.app.fairfree.api;

import com.app.fairfree.model.User;
import com.app.fairfree.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void verify_That_Get_Current_User_Is_Working() {
        String username = "test@example.com";
        User mockUser = new User();
        mockUser.setEmail(username);
        mockUser.setFullName("John Doe");
        mockUser.setRoles(Set.of());

        when(userService.getCurrentUser()).thenReturn(mockUser);
        ResponseEntity<@NotNull User> response = userController.getCurrentUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(username, response.getBody().getEmail());
        assertThat(response.getBody().getFullName(), equalTo(mockUser.getFullName()));
        verify(userService, times(1)).getCurrentUser();
    }

    @AfterEach
    void tearDown() {
        // clean up the security context
        SecurityContextHolder.clearContext();
    }

}
