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

@RestController
@RequestMapping("/api_BS/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CarpenterDTO dto) {
        CarpenterDTO response = authService.register(dto);
        return ResponseEntity.ok("Registro correcto "+"Bienvenido: "+response.getName() );
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
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

        return ResponseEntity.ok("Login successful");
    }


    @GetMapping("/me")
    public ResponseEntity<CarpenterDTO> getUser(@CookieValue(name = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        CarpenterDTO carpenterDTO = authService.getCarpenterFromToken(token);
        return ResponseEntity.ok(carpenterDTO);
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
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Sesión cerrada correctamente.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
