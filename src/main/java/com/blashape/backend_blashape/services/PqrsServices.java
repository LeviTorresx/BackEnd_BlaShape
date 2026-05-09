package com.blashape.backend_blashape.services;

import com.blashape.backend_blashape.DTOs.*;
import com.blashape.backend_blashape.config.JwtUtil;
import com.blashape.backend_blashape.entitys.*;
import com.blashape.backend_blashape.mapper.PqrsMapper;
import com.blashape.backend_blashape.repositories.CarpenterRepository;
import com.blashape.backend_blashape.repositories.CustomerRepository;
import com.blashape.backend_blashape.repositories.PqrsRepository;
import com.blashape.backend_blashape.repositories.WorkshopRepository;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PqrsServices {

    private static final int MAX_SUBJECT_LENGTH = 150;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final PqrsRepository pqrsRepository;
    private final CustomerRepository customerRepository;
    private final CarpenterRepository carpenterRepository;
    private final PqrsMapper pqrsMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final WorkshopRepository workshopRepository;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    // ============ CREATE (híbrido) ============
    public PqrsCreateResponse createPqrs(PqrsRequest request, String sessionToken) {
        validateBaseRequest(request);
        validateGuestFields(request);

        PqrsScope scope = request.getScope() != null ? request.getScope() : PqrsScope.WORKSHOP;

        Carpenter carpenter;
        Workshop  workshop = null;

        if (scope == PqrsScope.WORKSHOP) {
            if (request.getWorkshopId() == null && request.getCarpenterId() == null) {
                throw new IllegalArgumentException("Debe indicar el taller destinatario");
            }
            if (request.getWorkshopId() != null) {
                workshop = workshopRepository.findById(request.getWorkshopId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Taller no encontrado con ID: " + request.getWorkshopId()));
                carpenter = workshop.getCarpenter();
                if (carpenter == null) {
                    throw new IllegalStateException("El taller no tiene carpintero asignado");
                }
            } else {
                carpenter = carpenterRepository.findById(request.getCarpenterId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Carpintero no encontrado con ID: " + request.getCarpenterId()));
            }
        } else { // GENERAL
            carpenter = carpenterRepository
                    .findFirstByRoleAndIsActiveTrueOrderByCarpenterIdAsc(UserRole.PQRS_RECEIVER)
                    .orElseThrow(() -> new IllegalStateException(
                            "No hay receptor de PQRS generales configurado. Contacte al administrador."));
        }

        String email = request.getGuestEmail().trim().toLowerCase();

        Pqrs pqrs = new Pqrs();
        pqrs.setSubject(request.getSubject().trim());
        pqrs.setMessage(request.getMessage().trim());
        pqrs.setType(request.getType());
        pqrs.setScope(scope);
        pqrs.setStatus(PqrsStatus.PENDIENTE);
        pqrs.setCarpenter(carpenter);
        pqrs.setWorkshop(workshop);
        pqrs.setTrackingCode(generateTrackingCode());

        pqrs.setGuestName(request.getGuestName().trim());
        pqrs.setGuestLastName(safeTrim(request.getGuestLastName()));
        pqrs.setGuestEmail(email);
        pqrs.setGuestPhone(safeTrim(request.getGuestPhone()));

        customerRepository.findActiveByEmailAndCarpenterId(email, carpenter.getCarpenterId())
                .ifPresent(pqrs::setCustomer);

        Pqrs saved = pqrsRepository.save(pqrs);

        String trackingJwt = jwtUtil.generatePqrsTrackingToken(saved.getPqrsId(), email);
        String trackingLink = buildTrackingLink(trackingJwt);
        sendTrackingEmail(saved, trackingLink);

        PqrsCreateResponse response = new PqrsCreateResponse();
        response.setPqrs(pqrsMapper.toDTO(saved));
        response.setTrackingCode(saved.getTrackingCode());
        response.setTrackingLink(trackingLink);
        response.setLinkedToAccount(saved.getCustomer() != null);
        return response;
    }

    // ============ READ ============
    public PqrsDTO getPqrs(Long id) {
        Pqrs pqrs = pqrsRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("PQRS no encontrada con ID: " + id));
        return pqrsMapper.toDTO(pqrs);
    }

    /** Magic link: el JWT efímero permite ver UNA PQRS específica sin estar logueado. */
    public PqrsDTO getByTrackingToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de seguimiento no proporcionado");
        }
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new IllegalArgumentException("Token expirado o inválido");
            }
            Long pqrsId = jwtUtil.extractPqrsIdFromTrackingToken(token);
            Pqrs pqrs = pqrsRepository.findActiveById(pqrsId)
                    .orElseThrow(() -> new EntityNotFoundException("PQRS no encontrada"));
            return pqrsMapper.toDTO(pqrs);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token de seguimiento inválido");
        }
    }

    /** Búsqueda por código de radicado (requiere email para evitar enumeración). */
    public PqrsDTO getByTrackingCode(String trackingCode, String email) {
        if (trackingCode == null || trackingCode.isBlank()) {
            throw new IllegalArgumentException("El código de radicado es obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio para validar el acceso");
        }

        Pqrs pqrs = pqrsRepository.findActiveByTrackingCode(trackingCode.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró una PQRS con ese código"));

        String storedEmail = pqrs.getCustomer() != null
                ? pqrs.getCustomer().getEmail()
                : pqrs.getGuestEmail();

        if (storedEmail == null || !storedEmail.equalsIgnoreCase(email.trim())) {
            // Mensaje deliberadamente genérico — no revelamos si existe o no
            throw new EntityNotFoundException("No se encontró una PQRS con ese código");
        }
        return pqrsMapper.toDTO(pqrs);
    }

    public List<PqrsDTO> getPqrsByCarpenterToken(String token) {
        Carpenter carpenter = extractCarpenterFromToken(token);
        return pqrsRepository.findActiveByCarpenterId(carpenter.getCarpenterId())
                .stream().map(pqrsMapper::toDTO).toList();
    }

    public List<PqrsDTO> getPqrsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Cliente no encontrado con ID: " + customerId);
        }
        return pqrsRepository.findActiveByCustomerId(customerId)
                .stream().map(pqrsMapper::toDTO).toList();
    }

    // ============ RESPOND ============
    public PqrsDTO respondPqrs(Long id, PqrsAnswerRequest request, String token) {
        if (request.getResponse() == null || request.getResponse().isBlank()) {
            throw new IllegalArgumentException("La respuesta no puede estar vacía");
        }
        if (request.getResponse().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "La respuesta no puede superar " + MAX_MESSAGE_LENGTH + " caracteres");
        }

        Carpenter carpenter = extractCarpenterFromToken(token);
        Pqrs pqrs = pqrsRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("PQRS no encontrada con ID: " + id));

        if (!pqrs.getCarpenter().getCarpenterId().equals(carpenter.getCarpenterId())) {
            throw new SecurityException("No tienes permisos para responder esta PQRS");
        }
        if (pqrs.getStatus() == PqrsStatus.CERRADA) {
            throw new IllegalStateException("No se puede responder una PQRS cerrada");
        }

        pqrs.setResponse(request.getResponse().trim());
        pqrs.setRespondedAt(LocalDateTime.now());
        pqrs.setStatus(PqrsStatus.RESUELTA);

        Pqrs saved = pqrsRepository.save(pqrs);
        notifyResponse(saved);
        return pqrsMapper.toDTO(saved);
    }

    // ============ DELETE (soft) ============
    public void deletePqrs(Long id, String token) {
        Carpenter carpenter = extractCarpenterFromToken(token);
        Pqrs pqrs = pqrsRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("PQRS no encontrada con ID: " + id));

        if (!pqrs.getCarpenter().getCarpenterId().equals(carpenter.getCarpenterId())) {
            throw new SecurityException("No tienes permisos para eliminar esta PQRS");
        }
        pqrs.setDeleted(true);
        pqrs.setDeletedAt(LocalDateTime.now());
        pqrsRepository.save(pqrs);
    }

    // ============ AUTO-LINK al crear customer ============
    /**
     * Vincula PQRS huérfanas (creadas como invitado) cuando se registra un Customer
     * cuyo email coincide. Llamar desde CustomerService.createCustomer().
     */
    public int linkOrphanPqrsToCustomer(Customer customer) {
        if (customer == null || customer.getEmail() == null || customer.getCarpenter() == null) {
            return 0;
        }
        List<Pqrs> orphans = pqrsRepository.findOrphanByGuestEmail(
                customer.getEmail(), customer.getCarpenter().getCarpenterId());
        orphans.forEach(p -> p.setCustomer(customer));
        if (!orphans.isEmpty()) {
            pqrsRepository.saveAll(orphans);
        }
        return orphans.size();
    }

    // ============ Helpers ============
    private void validateBaseRequest(PqrsRequest request) {
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("El asunto es obligatorio");
        }
        if (request.getSubject().length() > MAX_SUBJECT_LENGTH) {
            throw new IllegalArgumentException(
                    "El asunto no puede superar " + MAX_SUBJECT_LENGTH + " caracteres");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio");
        }
        if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "El mensaje no puede superar " + MAX_MESSAGE_LENGTH + " caracteres");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException(
                    "El tipo de PQRS es obligatorio (PETICION, QUEJA, RECLAMO o SUGERENCIA)");
        }
    }

    private void validateGuestFields(PqrsRequest request) {
        if (request.getGuestName() == null || request.getGuestName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio para usuarios no registrados");
        }
        if (request.getGuestEmail() == null || request.getGuestEmail().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio para usuarios no registrados");
        }
        if (!request.getGuestEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El formato del correo es inválido");
        }
    }


    private void ensureCustomerBelongsToCarpenter(Customer customer, Carpenter carpenter) {
        if (customer.getCarpenter() == null
                || !customer.getCarpenter().getCarpenterId().equals(carpenter.getCarpenterId())) {
            throw new IllegalArgumentException(
                    "El cliente no está asociado al carpintero indicado");
        }
    }

    private Carpenter extractCarpenterFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token no proporcionado");
        }
        String email = jwtUtil.extractEmail(token);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }
        return carpenterRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Carpintero no encontrado para el token"));
    }

    private String generateTrackingCode() {
        String code;
        do {
            code = "PQRS-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 8).toUpperCase();
        } while (pqrsRepository.findActiveByTrackingCode(code).isPresent());
        return code;
    }

    private String buildTrackingLink(String jwt) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/pqrs/seguimiento?token=" + jwt;
    }

    private void sendTrackingEmail(Pqrs pqrs, String link) {
        try {
            String to = pqrs.getCustomer() != null ? pqrs.getCustomer().getEmail() : pqrs.getGuestEmail();
            String subject = "Hemos recibido tu PQRS — Radicado " + pqrs.getTrackingCode();
            String body = """
                    Hola %s,

                    Tu PQRS fue radicada con el código: %s

                    Puedes hacer seguimiento en cualquier momento desde este enlace:
                    %s

                    Asunto: %s
                    Estado actual: %s

                    Te notificaremos por correo cuando recibas una respuesta.
                    """.formatted(
                    nameFor(pqrs),
                    pqrs.getTrackingCode(),
                    link,
                    pqrs.getSubject(),
                    pqrs.getStatus()
            );
            emailService.sendEmail(to, subject, body);
        } catch (Exception ex) {
            // No queremos que un fallo de email tumbe la creación
            // (idealmente: log + métrica)
        }
    }

    private void notifyResponse(Pqrs pqrs) {
        try {
            String to = pqrs.getCustomer() != null ? pqrs.getCustomer().getEmail() : pqrs.getGuestEmail();
            if (to == null) return;
            String subject = "Tu PQRS " + pqrs.getTrackingCode() + " tiene respuesta";
            String body = "Hola " + nameFor(pqrs) + ",\n\nHemos respondido tu PQRS:\n\n"
                    + pqrs.getResponse() + "\n\nGracias por contactarnos.";
            emailService.sendEmail(to, subject, body);
        } catch (Exception ignored) { /* log */ }
    }

    private String nameFor(Pqrs pqrs) {
        if (pqrs.getCustomer() != null) return pqrs.getCustomer().getName();
        return pqrs.getGuestName() != null ? pqrs.getGuestName() : "";
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}