package com.app.fairfree.integration;

import com.app.fairfree.dto.SignupRequest;
import com.app.fairfree.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("fairfree_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void login_with_valid_credentials_returns_jwt() throws Exception {
        // Thread.sleep(600000); 10 min, sleeping for container inspection as containers are ephemeral.
        SignupRequest userRequest = new SignupRequest("John Doe", "john@test.com", "password123");
        // First register the user and login with the same credentials
        userService.registerUser(userRequest);

        // Create JSON request for user login (the same credentials as registration)
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("email", userRequest.email());
        requestMap.put("password", userRequest.password());

        String requestBody = mapper.writeValueAsString(requestMap);
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .content(requestBody)
                                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(userRequest.email()))
                .andExpect(jsonPath("$.password").doesNotExist());

    }
}
