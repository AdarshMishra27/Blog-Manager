package com.example.BlogManager.controllers;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.objects.UserType;
import com.example.BlogManager.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class) // 1. Slice test: Only loads UserController
@AutoConfigureMockMvc(addFilters = false) // 2. Security: Disable Spring Security filters (Login/Auth) for this unit test
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // --- THE NEW ANNOTATION ---
    // Replaces @MockBean. It tells Spring: "Override the real UserService bean
    // in the context with this Mockito mock."
    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Converts Java Objects -> JSON

    @Test
    void register_ShouldReturn201_WhenUserCreatedSuccessfully() throws Exception {
        //AAA -> pattern in testing -> ARRANGE (GIVEN) -> ACT (WHEN) -> ASSERT(THEN)
        // ==========================================
        // 1. ARRANGE (Prepare the data & mocks)
        // ==========================================
        UserDTO inputUser = new UserDTO("John", "john123", "pass", UserType.ADMIN, null);
        UserDTO savedUser = new UserDTO("John", "john123", null, UserType.ADMIN, null);

        // Train the mock: "When someone calls register, return this specific object"
        when(userService.register(any(UserDTO.class))).thenReturn(savedUser);


        // ==========================================
        // 2. ACT (Run the method under test)
        // ==========================================
        // In a Controller test, "performing" the request is the Action.
        ResultActions result = mockMvc.perform(post("/api/user/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputUser)));


        // ==========================================
        // 3. ASSERT (Verify the results)
        // ==========================================
        result
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Successfully Created!"))
                .andExpect(jsonPath("$.data.userId").value("john123"));
    }

    @Test
    void register_ShouldReturn409_WhenUsernameExists() throws Exception {
        // GIVEN
        UserDTO inputUser = new UserDTO("John Doe", "john", "password", UserType.USER, null);

        List<String> suggestions = List.of("john1234", "john2893");
        UserDTO conflictUser = new UserDTO("John Doe", "john", null, UserType.USER, suggestions);

        when(userService.register(any(UserDTO.class))).thenReturn(conflictUser);

        // WHEN & THEN
        mockMvc.perform(post("/api/user/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("username already exists"));
    }

    @Test
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        // GIVEN
        UserDTO loginRequest = new UserDTO(null, "john123", "password", null, null);
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.fake.token";

        when(userService.login(any(UserDTO.class))).thenReturn(fakeToken);

        // WHEN & THEN
        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(fakeToken));
    }

    @Test
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        // GIVEN
        String userId = "user123";
        UserEntity mockEntity = new UserEntity();
        mockEntity.setName("Test User");
        mockEntity.setUserType(UserType.USER);

        when(userService.findById(userId)).thenReturn(Optional.of(mockEntity));

        // WHEN & THEN
        mockMvc.perform(get("/api/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    void getUserById_ShouldThrow404_WhenUserDoesNotExist() throws Exception {
        // GIVEN
        String userId = "unknown";
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get("/api/user/{userId}", userId))
                .andExpect(status().isNotFound());
        // Note: Ensure GlobalExceptionHandler maps ResourceNotFoundCustomException to 404
    }
}
