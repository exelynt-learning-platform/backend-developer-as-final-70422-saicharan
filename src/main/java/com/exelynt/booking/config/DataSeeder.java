package com.exelynt.booking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.exelynt.booking.entity.Role;
import com.exelynt.booking.entity.User;
import com.exelynt.booking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@example.com")) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .build());
        }

        if (!userRepository.existsByEmail("user@example.com")) {
            userRepository.save(User.builder()
                    .name("Test User")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("User@123"))
                    .role(Role.USER)
                    .build());
        }
    }
}
