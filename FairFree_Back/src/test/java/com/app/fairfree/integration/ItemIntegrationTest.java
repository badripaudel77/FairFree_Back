package com.app.fairfree.integration;

import com.app.fairfree.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String email = "new@example.com";
    private final String password = "password123";


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

    @BeforeEach
    void setupUser() throws Exception {
        SignupRequest signup = new SignupRequest("New User", email, password);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());
    }

    @Test
    void addAndGetAvailableItems() throws Exception {
        // Login user and get JWT token
        LoginRequest login = new LoginRequest(email, password);
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> loginMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) loginMap.get("token");

        LocationRequest location = new LocationRequest("123 Main Street", "New York", "NY", "USA", 40.7128, -74.0060);
        ItemRequest itemRequest = new ItemRequest("Test Item", "This is a test item", 10, false, 30, location, null);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "itemToBeAddedString", "",
                MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(itemRequest)
        );
        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "dummy.jpg",
                MediaType.IMAGE_JPEG_VALUE, "fake-image-content".getBytes()
        );

        // Add item via multipart POST
        mockMvc.perform(multipart("/api/v1/items")
                        .file(jsonPart)
                        .file(imageFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.location.city").value("New York"))
                .andExpect(jsonPath("$.owner.email").value(email))
                .andExpect(jsonPath("$.receiver").doesNotExist());

        // Retrieve all available items
        mockMvc.perform(get("/api/v1/items/available")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Item"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].expiresAfterDays").value(30))
                .andExpect(jsonPath("$[0].location.city").value("New York"))
                .andExpect(jsonPath("$[0].owner.email").value(email))
                .andExpect(jsonPath("$[0].receiver").doesNotExist());
    }
}
