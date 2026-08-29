package com.exelynt.booking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.exelynt.booking.AbstractIntegrationTest;
import com.exelynt.booking.dto.LoginRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void loginWithValidAdminCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("admin@example.com", "Admin@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginWithValidUserCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "User@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void loginWithWrongPassword_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "WrongPassword1");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithMissingEmail_returnsBadRequest() throws Exception {
        LoginRequest request = new LoginRequest("", "User@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
