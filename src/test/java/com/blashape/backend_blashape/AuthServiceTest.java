package com.blashape.backend_blashape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.mapper.CarpenterMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CarpenterRepository carpenterRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CarpenterMapper carpenterMapper;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthService authService;

    private Carpenter carpenter;
    private CarpenterDTO carpenterDTO;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        carpenter = new Carpenter();
        carpenter.setCarpenterId(1L);
        carpenter.setName("Juan");
        carpenter.setLastName("Pérez");
        carpenter.setDni("12345678");
        carpenter.setEmail("juan.perez@gmail.com");
        carpenter.setPassword("$2a$10$encodedPassword");
        carpenter.setPhone("123456789");
        carpenter.setRole(UserRole.CARPENTER);

        carpenterDTO = new CarpenterDTO();
        carpenterDTO.setCarpenterId(1L);
        carpenterDTO.setName("Juan");
        carpenterDTO.setLastName("Pérez");
        carpenterDTO.setDni("12345678");
        carpenterDTO.setEmail("juan.perez@gmail.com");
        carpenterDTO.setPassword("password123");
        carpenterDTO.setPhone("123456789");
        carpenterDTO.setRole("CARPENTER");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan.perez@gmail.com");
        loginRequest.setPassword("password123");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void loginSuccess() {
        // Arrange
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));
        when(passwordEncoder.matches("password123", carpenter.getPassword()))
                .thenReturn(true);
        when(jwtUtil.generateToken("juan.perez@gmail.com", 1L, "PRO"))
                .thenReturn("mocked-jwt-token");

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        verify(carpenterRepository).findByEmail("juan.perez@gmail.com");
        verify(passwordEncoder).matches("password123", carpenter.getPassword());
        verify(jwtUtil).generateToken("juan.perez@gmail.com", 1L, "PRO");
    }

    @Test
    void loginUserNotFound() {
        // Arrange
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
        assertEquals("Cuenta no encontrada", exception.getMessage());
        verify(carpenterRepository).findByEmail("juan.perez@gmail.com");
    }

    @Test
    void loginIncorrectPassword() {
        // Arrange
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));
        when(passwordEncoder.matches("password123", carpenter.getPassword()))
                .thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
        assertEquals("Contraseña incorrecta", exception.getMessage());
        verify(passwordEncoder).matches("password123", carpenter.getPassword());
    }

    // ==================== REGISTER TESTS ====================

    @Test
    void registerSuccess() {
        // Arrange
        when(carpenterRepository.existsByEmail(carpenterDTO.getEmail())).thenReturn(false);
        when(carpenterRepository.existsByDni(carpenterDTO.getDni())).thenReturn(false);
        when(carpenterMapper.toEntity(carpenterDTO)).thenReturn(carpenter);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(carpenterRepository.save(any(Carpenter.class))).thenReturn(carpenter);
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        // Act
        CarpenterDTO result = authService.register(carpenterDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertNull(result.getPassword());
        verify(carpenterRepository).existsByEmail("juan.perez@gmail.com");
        verify(carpenterRepository).existsByDni("12345678");
        verify(passwordEncoder).encode("password123");
        verify(carpenterRepository).save(any(Carpenter.class));
    }

    @Test
    void registerWithNullName() {
        // Arrange
        carpenterDTO.setName(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("El name es obligatorio", exception.getMessage());
    }

    @Test
    void registerWithBlankLastName() {
        // Arrange
        carpenterDTO.setLastName("   ");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("El apellido es obligatorio", exception.getMessage());
    }

    @Test
    void registerWithInvalidEmail() {
        // Arrange
        carpenterDTO.setEmail("correo-invalido");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("El formato del correo es inválido", exception.getMessage());
    }

    @Test
    void registerWithShortPassword() {
        // Arrange
        carpenterDTO.setPassword("12345");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
    }

    @Test
    void registerWithExistingEmail() {
        // Arrange
        when(carpenterRepository.existsByEmail("juan.perez@gmail.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(carpenterRepository).existsByEmail("juan.perez@gmail.com");
    }

    @Test
    void registerWithExistingDni() {
        // Arrange
        when(carpenterRepository.existsByEmail("juan.perez@gmail.com")).thenReturn(false);
        when(carpenterRepository.existsByDni("12345678")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(carpenterDTO));
        assertEquals("La cédula ya está registrada", exception.getMessage());
        verify(carpenterRepository).existsByDni("12345678");
    }

    // ==================== GET CARPENTER FROM TOKEN TESTS ====================

    @Test
    void getCarpenterFromTokenSuccess() {
        // Arrange
        String token = "valid-jwt-token";
        when(jwtUtil.extractEmail(token)).thenReturn("juan.perez@gmail.com");
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        // Act
        CarpenterDTO result = authService.getCarpenterFromToken(token);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.getName());
        assertNull(result.getPassword());
        verify(jwtUtil).extractEmail(token);
        verify(carpenterRepository).findByEmail("juan.perez@gmail.com");
    }

    @Test
    void getCarpenterFromTokenWithNullToken() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.getCarpenterFromToken(null));
        assertEquals("Token missing", exception.getMessage());
    }

    @Test
    void getCarpenterFromTokenWithEmptyToken() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.getCarpenterFromToken(""));
        assertEquals("Token missing", exception.getMessage());
    }

    @Test
    void getCarpenterFromTokenUserNotFound() {
        // Arrange
        String token = "valid-jwt-token";
        when(jwtUtil.extractEmail(token)).thenReturn("juan.perez@gmail.com");
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.getCarpenterFromToken(token));
        assertEquals("User not found", exception.getMessage());
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    void updateProfileSuccess() {
        // Arrange
        String token = "valid-jwt-token";
        CarpenterDTO updateDTO = new CarpenterDTO();
        updateDTO.setName("Juan Carlos");
        updateDTO.setPhone("987654321");

        when(jwtUtil.extractEmail(token)).thenReturn("juan.perez@gmail.com");
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));
        when(carpenterRepository.save(any(Carpenter.class))).thenReturn(carpenter);
        when(carpenterMapper.toDTO(carpenter)).thenReturn(carpenterDTO);

        // Act
        CarpenterDTO result = authService.updateProfile(token, 1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(carpenterRepository).save(carpenter);
    }

    @Test
    void updateProfileWithDifferentUserId() {
        // Arrange
        String token = "valid-jwt-token";
        CarpenterDTO updateDTO = new CarpenterDTO();
        updateDTO.setName("Nuevo Nombre");

        when(jwtUtil.extractEmail(token)).thenReturn("juan.perez@gmail.com");
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> authService.updateProfile(token, 999L, updateDTO));
        assertEquals("No tienes permisos para modificar este perfil", exception.getMessage());
    }

    @Test
    void updateProfileWithPasswordAttempt() {
        // Arrange
        String token = "valid-jwt-token";
        CarpenterDTO updateDTO = new CarpenterDTO();
        updateDTO.setPassword("newPassword");

        when(jwtUtil.extractEmail(token)).thenReturn("juan.perez@gmail.com");
        when(carpenterRepository.findByEmail("juan.perez@gmail.com"))
                .thenReturn(Optional.of(carpenter));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.updateProfile(token, 1L, updateDTO));
        assertEquals("No se permite modificar la contraseña desde esta ruta", exception.getMessage());
    }

    // ==================== UPDATE PASSWORD TESTS ====================

    @Test
    void updatePasswordSuccess() {
        // Arrange
        String email = "juan.perez@gmail.com";
        String newPassword = "newSecurePassword";
        when(carpenterRepository.findByEmail(email)).thenReturn(Optional.of(carpenter));
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newEncodedPassword");
        when(carpenterRepository.save(any(Carpenter.class))).thenReturn(carpenter);

        // Act
        authService.updatePassword(email, newPassword);

        // Assert
        verify(carpenterRepository).findByEmail(email);
        verify(passwordEncoder).encode(newPassword);
        verify(carpenterRepository).save(carpenter);
    }

    @Test
    void updatePasswordUserNotFound() {
        // Arrange
        String email = "nonexistent@gmail.com";
        when(carpenterRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.updatePassword(email, "newPassword"));
        assertEquals("Usuario no encontrado con email: nonexistent@gmail.com", exception.getMessage());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    void logoutSuccess() {
        // Act
        authService.logout(httpServletResponse);

        // Assert
        verify(httpServletResponse).addCookie(any(Cookie.class));
    }
}
