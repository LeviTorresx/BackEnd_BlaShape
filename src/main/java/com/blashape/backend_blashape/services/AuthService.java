package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.EmailVerificationToken;
import com.blashape.backend_blashape.entitys.UserRole;
import com.blashape.backend_blashape.mapper.CarpenterMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.EmailVerificationTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final CarpenterRepository carpenterRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CarpenterMapper carpenterMapper;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final MonetizationService monetizationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public LoginResponse login(LoginRequest request) {
        Carpenter carpenter = carpenterRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (!passwordEncoder.matches(request.getPassword(), carpenter.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        if (!carpenter.getEmailVerified()) {
            throw new RuntimeException("Debes verificar tu correo antes de iniciar sesión");
        }

        String plan = monetizationService.getActivePlanByCarpenterId(carpenter.getCarpenterId()).getPlan().getPlanName();

        String token = jwtUtil.generateToken(carpenter.getEmail(), carpenter.getCarpenterId(), plan);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return response;
    }

    private String buildVerificationEmail(String name, String token) {

        String html = """
            <div style="font-family: Arial, sans-serif; text-align: center; padding: 20px;">
              
              <img src="{{LOGO_URL}}" alt="Blashape Logo" 
                   style="width: 120px; margin-bottom: 20px;" />
        
              <h2 style="color: #4c1d95;">Verifica tu correo</h2>
        
              <p>Hola {{NAME}},</p>
        
              <p>Gracias por registrarte en Blashape.</p>
              <p>Haz clic en el botón para verificar tu cuenta:</p>
        
              <a href="{{LINK}}"
                 style="
                    display: inline-block;
                    padding: 12px 24px;
                    margin-top: 20px;
                    background-color: #4c1d95;
                    color: white;
                    text-decoration: none;
                    border-radius: 8px;
                    font-weight: bold;
                 ">
                 Verificar correo
              </a>
        
              <p style="margin-top: 20px; font-size: 12px; color: gray;">
                Este enlace expirará en 24 horas.
              </p>
        
              <p style="font-size: 12px; color: gray;">
                Si no creaste esta cuenta, ignora este mensaje.
              </p>
            </div>
            """;

        String link = frontendUrl + "/verify-email?token=" + token;
        String logoUrl = "https://res.cloudinary.com/dr63i7owa/image/upload/v1773726191/logo_BS_uxob0a.png";

        // reemplazos dinámicos
        html = html.replace("{{NAME}}", name);
        html = html.replace("{{LINK}}", link);
        html = html.replace("{{LOGO_URL}}", logoUrl);

        return html;
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

        if (carpenterRepository.existsByDni(dto.getDni())) {
            throw new IllegalArgumentException("La cédula ya está registrada");
        }

        Carpenter existing = carpenterRepository.findByEmail(dto.getEmail()).orElse(null);

        if (existing != null) {

            if (existing.getEmailVerified()) {
                throw new IllegalArgumentException("El correo ya está registrado");
            }

            // Si existe pero NO está verificado, reenviar verificación
            createAndSendVerificationToken(existing);

            CarpenterDTO response = carpenterMapper.toDTO(existing);
            response.setPassword(null);
            return response;
        }

        Carpenter carpenter = carpenterMapper.toEntity(dto);

        carpenter.setPassword(passwordEncoder.encode(dto.getPassword()));
        carpenter.setWorkshop(null);
        carpenter.setRole(UserRole.CARPENTER);
        carpenter.setIsActive(true);
        carpenter.setEmailVerified(false);

        Carpenter saved = carpenterRepository.save(carpenter);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = new EmailVerificationToken();

        verificationToken.setToken(token);
        verificationToken.setCarpenter(saved);
        verificationToken.setExpirationDate(
                Instant.now().plus(24, ChronoUnit.HOURS)
        );

        tokenRepository.save(verificationToken);

        String emailBody = buildVerificationEmail(saved.getName(), token);

        emailService.sendEmail(
                saved.getEmail(),
                "Verificación de cuenta - Blashape",
                emailBody
        );

        CarpenterDTO response = carpenterMapper.toDTO(saved);
        response.setWorkshop(null);
        response.setPassword(null);
        return response;
    }

    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (verificationToken.getExpirationDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token expirado");
        }

        Carpenter carpenter = verificationToken.getCarpenter();

        carpenter.setEmailVerified(true);

        carpenterRepository.save(carpenter);

        tokenRepository.delete(verificationToken);
    }

    private void createAndSendVerificationToken(Carpenter carpenter) {

        tokenRepository.deleteAll(
                tokenRepository.findByCarpenter(carpenter)
        );

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = new EmailVerificationToken();

        verificationToken.setToken(token);
        verificationToken.setCarpenter(carpenter);
        verificationToken.setExpirationDate(
                Instant.now().plus(24, ChronoUnit.HOURS)
        );

        tokenRepository.save(verificationToken);

        String emailBody = buildVerificationEmail(carpenter.getName(), token);

        emailService.sendEmail(
                carpenter.getEmail(),
                "Verificación de cuenta - Blashape",
                emailBody
        );
    }

    public void resendVerificationEmail(String email) {

        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (carpenter.getEmailVerified()) {
            throw new RuntimeException("El correo ya está verificado");
        }

        createAndSendVerificationToken(carpenter);
    }

    public CarpenterDTO getCarpenterFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token missing");
        }

        String email = jwtUtil.extractEmail(token);

        Carpenter user = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CarpenterDTO dto = carpenterMapper.toDTO(user);
        dto.setPassword(null);
        return dto;
    }

    public CarpenterDTO updateProfile(String token, Long id, CarpenterDTO dto) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token missing");
        }

        String email = jwtUtil.extractEmail(token);

        Carpenter existing = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("No se permite modificar la contraseña desde esta ruta");
        }

        if (!existing.getCarpenterId().equals(id)) {
            throw new SecurityException("No tienes permisos para modificar este perfil");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            existing.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("El formato del correo es inválido");
            }
            if (!dto.getEmail().equals(existing.getEmail()) &&
                    carpenterRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El correo ya está en uso");
            }
            existing.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            existing.setPhone(dto.getPhone());
        }

        Carpenter saved = carpenterRepository.save(existing);
        CarpenterDTO updated = carpenterMapper.toDTO(saved);
        updated.setPassword(null);

        return updated;
    }


    public void updatePassword(String email, String newPassword) {

        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado con email: " + email));

        String currentPasswordHash = carpenter.getPassword();

        // Evitar misma contraseña
        if (passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new IllegalArgumentException(
                    "La nueva contraseña no puede ser igual a la actual");
        }

        // Reglas de seguridad
        if (!newPassword.matches("^(?=.*[A-Z])(?=.*[a-z]).{8,}$")) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula y una minúscula");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        carpenter.setPassword(encodedPassword);

        carpenterRepository.save(carpenter);
    }

    public void changePassword(String token, String currentPassword, String newPassword) {

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token missing");
        }

        String email = jwtUtil.extractEmail(token);

        Carpenter carpenter = carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // validar contraseña actual
        if (!passwordEncoder.matches(currentPassword, carpenter.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // evitar reutilizar contraseña
        if (passwordEncoder.matches(newPassword, carpenter.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
        }

        // reglas de seguridad
        if (!newPassword.matches("^(?=.*[A-Z])(?=.*[a-z]).{8,}$")) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula y una minúscula"
            );
        }

        carpenter.setPassword(passwordEncoder.encode(newPassword));

        carpenterRepository.save(carpenter);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // usar true en producción con HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(0); // expira inmediatamente
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }
}
