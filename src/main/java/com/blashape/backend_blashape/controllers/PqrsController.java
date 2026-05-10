package com.blashape.backend_blashape.controllers;

import com.blashape.backend_blashape.DTOs.*;
import com.blashape.backend_blashape.services.PqrsServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api_BS/pqrs")
@RequiredArgsConstructor
public class PqrsController {

    private final PqrsServices pqrsServices;
    private static final String M_KEY = "message";

    @PostMapping("/create")
    public ResponseEntity<PqrsCreateResponse> createPqrs(@RequestBody PqrsRequest request) {
        PqrsCreateResponse created = pqrsServices.createPqrs(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Magic link — JWT efímero recibido por correo. */
    @GetMapping("/track")
    public ResponseEntity<PqrsDTO> trackByMagicLink(@RequestParam("token") String token) {
        return ResponseEntity.ok(pqrsServices.getByTrackingToken(token));
    }

    /** Seguimiento manual con código de radicado + correo. */
    @GetMapping("/track-by-code")
    public ResponseEntity<PqrsDTO> trackByCode(
            @RequestParam("code") String code,
            @RequestParam("email") String email) {
        return ResponseEntity.ok(pqrsServices.getByTrackingCode(code, email));
    }

    /** Listado del carpintero (requiere sesión). */
    @GetMapping("/all")
    public ResponseEntity<List<PqrsDTO>> getAllByCarpenter(
            @CookieValue(name = "jwt", required = false) String token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(pqrsServices.getPqrsByCarpenterToken(token));
    }

    /** Historial de un cliente (para mostrar en su panel cuando se loguee). */
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<PqrsDTO>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(pqrsServices.getPqrsByCustomerId(customerId));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<PqrsDTO> getPqrs(
            @PathVariable Long id,
            @CookieValue(name = "jwt", required = false) String token) {
        return ResponseEntity.ok(pqrsServices.getPqrs(id, token));
    }

    @PutMapping("/respond/{id}")
    public ResponseEntity<Map<String, Object>> respondPqrs(
            @PathVariable Long id,
            @RequestBody PqrsAnswerRequest request,
            @CookieValue(name = "jwt", required = false) String token) {
        if (token == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        PqrsDTO updated = pqrsServices.respondPqrs(id, request, token);
        return ResponseEntity.ok(Map.of(M_KEY, "Respuesta enviada exitosamente", "pqrs", updated));
    }
}