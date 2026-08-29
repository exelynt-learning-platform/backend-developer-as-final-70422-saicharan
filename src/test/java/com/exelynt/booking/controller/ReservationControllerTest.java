package com.exelynt.booking.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.exelynt.booking.AbstractIntegrationTest;
import com.exelynt.booking.dto.ReservationRequest;
import com.exelynt.booking.entity.Resource;
import com.exelynt.booking.entity.ResourceType;
import com.exelynt.booking.entity.Role;
import com.exelynt.booking.entity.User;
import com.exelynt.booking.repository.ResourceRepository;
import com.exelynt.booking.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Resource testResource;

    @BeforeEach
    void setUp() {
        testResource = resourceRepository.save(Resource.builder()
                .name("Conference Room")
                .description("A room for meetings")
                .type(ResourceType.ROOM)
                .price(new BigDecimal("100.00"))
                .available(true)
                .build());

        if (!userRepository.existsByEmail("other@example.com")) {
            userRepository.save(User.builder()
                    .name("Other User")
                    .email("other@example.com")
                    .password(passwordEncoder.encode("Other@123"))
                    .role(Role.USER)
                    .build());
        }
    }

    @Test
    void userCanCreateReservation() throws Exception {
        String token = loginAndGetToken("user@example.com", "User@123");

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(testResource.getId())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userEmail").value("user@example.com"));
    }

    @Test
    void endTimeBeforeStartTime_returnsBadRequest() throws Exception {
        String token = loginAndGetToken("user@example.com", "User@123");

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(testResource.getId())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now())
                .price(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reservationForNonExistentResource_returnsNotFound() throws Exception {
        String token = loginAndGetToken("user@example.com", "User@123");

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(999999L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotAccessAnotherUsersReservation() throws Exception {
        String otherToken = loginAndGetToken("other@example.com", "Other@123");

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(testResource.getId())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("150.00"))
                .build();

        String responseBody = mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long reservationId = objectMapper.readTree(responseBody).get("id").asLong();

        String userToken = loginAndGetToken("user@example.com", "User@123");

        mockMvc.perform(get("/reservations/" + reservationId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanViewAllReservations() throws Exception {
        String userToken = loginAndGetToken("user@example.com", "User@123");

        ReservationRequest request = ReservationRequest.builder()
                .resourceId(testResource.getId())
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(new BigDecimal("120.00"))
                .build();

        mockMvc.perform(post("/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        String adminToken = loginAndGetToken("admin@example.com", "Admin@123");

        mockMvc.perform(get("/reservations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
