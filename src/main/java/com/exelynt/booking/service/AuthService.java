package com.exelynt.booking.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.exelynt.booking.dto.LoginRequest;
import com.exelynt.booking.dto.LoginResponse;
import com.exelynt.booking.exception.InvalidCredentialsException;
import com.exelynt.booking.security.JwtService;
import com.exelynt.booking.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authResult;

        try {
            authResult = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        String token = jwtService.generateToken(principal);

        return LoginResponse.builder()
                .token(token)
                .email(principal.getUsername())
                .role(principal.getUser().getRole())
                .build();
    }
}
