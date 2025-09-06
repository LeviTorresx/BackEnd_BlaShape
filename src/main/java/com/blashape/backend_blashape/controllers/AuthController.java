package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.CarpenterDTO;
import com.blashape.backend_blashape.DTOs.LoginRequest;
import com.blashape.backend_blashape.DTOs.LoginResponse;
import com.blashape.backend_blashape.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api_BS/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
