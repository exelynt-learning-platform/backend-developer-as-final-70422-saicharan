package com.exelynt.booking.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.exelynt.booking.AbstractIntegrationTest;
import com.exelynt.booking.dto.ResourceRequest;
import com.exelynt.booking.entity.ResourceType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceControllerTest extends AbstractIntegrationTest {

    @Test
    void adminCanCreateResource() throws Exception {
        String token = loginAndGetToken("admin@example.com", "Admin@123");

        ResourceRequest request = ResourceRequest.builder()
                .name("Conference Room A")
                .description("A room for meetings")
                .type(ResourceType.ROOM)
                .price(new BigDecimal("100.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void userCannotCreateResource() throws Exception {
        String token = loginAndGetToken("user@example.com", "User@123");

        ResourceRequest request = ResourceRequest.builder()
                .name("Company Car")
                .type(ResourceType.VEHICLE)
                .price(new BigDecimal("50.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanReadResources() throws Exception {
        String token = loginAndGetToken("user@example.com", "User@123");

        mockMvc.perform(get("/resources")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isUnauthorized());
    }
}
