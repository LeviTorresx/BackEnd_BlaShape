package com.blashape.backend_blashape.service;

import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.mapper.CarpenterMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AuthServiceTest {

    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CarpenterMapper carpenterMapper;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void loginWithValidCredentials_shouldReturnToken() {
        // Arrange
        LoginRequest request = new LoginRequest("admin@example.com", "admin123");
        System.out.println("Intentando login con email: " + request.getEmail() + " y contraseña: " + request.getPassword());

        Carpenter carpenter = new Carpenter();
        carpenter.setEmail("admin@example.com");
        carpenter.setPassword("encoded_admin123");
        carpenter.setCarpenterId(42L);
        System.out.println("Carpintero encontrado en base de datos: ID=" + carpenter.getCarpenterId() + ", Email=" + carpenter.getEmail());

        when(carpenterRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(carpenter));
        when(passwordEncoder.matches("admin123", "encoded_admin123")).thenReturn(true);
        when(jwtUtil.generateToken("admin@example.com", 42L)).thenReturn("jwt-token-42");

        // Act
        LoginResponse response = authService.login(request);
        System.out.println("Login exitoso. Token generado: " + response.getToken());

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-42", response.getToken());
        System.out.println("Verificación completada: el token coincide con el esperado.");
    }

}
