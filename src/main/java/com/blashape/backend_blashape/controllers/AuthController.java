package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.services.AuthService;
import com.blashape.backend_blashape.services.TwoFactorAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api_BS/auth")
@CrossOrigin("http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtil jwtUtil;
    private final String mKey = "message";

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody CarpenterDTO dto) {
        CarpenterDTO response = authService.register(dto);
        return ResponseEntity.ok(Map.of(mKey,"Registro correcto "+"Bienvenido: "+response.getName()) );
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request);

        Cookie cookie = new Cookie("jwt", loginResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(mKey, "Inicio de sesión exitoso"));

    }

    @GetMapping("/me")
    public ResponseEntity<CarpenterDTO> getUser(@CookieValue(name = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        CarpenterDTO carpenterDTO = authService.getCarpenterFromToken(token);
        return ResponseEntity.ok(carpenterDTO);
    }

    @PutMapping("/update-profile/{id}")
    public ResponseEntity<CarpenterDTO> updateProfile(
            @CookieValue(name = "jwt", required = false) String token,
            @PathVariable Long id,
            @RequestBody CarpenterDTO dto) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        CarpenterDTO updated = authService.updateProfile(token, id, dto);
        return ResponseEntity.ok(updated);
    }


    @PostMapping("/send-2fa")
    public ResponseEntity<String> send2FA(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromCookie(request);
        String email = jwtUtil.extractEmail(token);

        twoFactorAuthService.sendVerificationCode(email);
        return ResponseEntity.ok("Código enviado al correo asociado a tu cuenta.");
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<String> verify2FA(
            HttpServletRequest request,
            @RequestParam String code) {

        String token = jwtUtil.extractTokenFromCookie(request);
        String email = jwtUtil.extractEmail(token);

        if (twoFactorAuthService.verifyCode(email, code)) {
            return ResponseEntity.ok("Código correcto, puedes resetear tu contraseña.");
        }
        return ResponseEntity.status(401).body("Código incorrecto.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            HttpServletRequest request,
            @RequestParam String newPassword) {

        String token = jwtUtil.extractTokenFromCookie(request);
        String email = jwtUtil.extractEmail(token);

        if (!twoFactorAuthService.isVerified(email)) {
            return ResponseEntity.status(403).body("No tienes verificación válida.");
        }

        authService.updatePassword(email, newPassword);
        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>>  logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(Map.of(mKey, "Sesión cerrada exitosamente"));
    }
}
