package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private CarpenterRepository carpenterRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private ObjectMapper objectMapper;

    public LoginResponse login(LoginRequest request) {
        Carpenter carpenter = carpenterRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), carpenter.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(carpenter.getEmail(), carpenter.getCarpenterId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return response;
    }



}
