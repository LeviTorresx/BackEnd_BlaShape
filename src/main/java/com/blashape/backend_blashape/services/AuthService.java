package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CarpenterRepository carpenterRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

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

    public CarpenterDTO register(CarpenterDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }
        if (dto.getDni() == null || dto.getDni().isBlank()) {
            throw new IllegalArgumentException("La cédula es obligatoria");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }

        if (dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        if (carpenterRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (carpenterRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        Carpenter carpenter = objectMapper.convertValue(dto, Carpenter.class);

        carpenter.setPassword(passwordEncoder.encode(dto.getPassword()));

        carpenter.setWorkshop(null);
        carpenter.setRole(UserRole.CARPENTER);

        Carpenter saved = carpenterRepository.save(carpenter);

        CarpenterDTO response = objectMapper.convertValue(saved, CarpenterDTO.class);
        response.setWorkshopId(null);
        response.setPassword(null);
        return response;
    }

    public CarpenterDTO getCarpenterFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token missing");
        }

        String email = jwtUtil.extractEmail(token);

        Carpenter user = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CarpenterDTO dto = objectMapper.convertValue(user, CarpenterDTO.class);
        dto.setPassword(null);
        return dto;
    }

    public void updatePassword(String email, String newPassword) {
        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        String encodedPassword = passwordEncoder.encode(newPassword);
        carpenter.setPassword(encodedPassword);

        carpenterRepository.save(carpenter);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // usar true en producción con HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // expira inmediatamente

        response.addCookie(cookie);
    }

}
