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
import jakarta.transaction.Transactional;
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

    private static final String BRAND_PURPLE  = "#581c87";
    private static final String BRAND_ACCENT  = "#7e22ce";
    private static final String BRAND_BG      = "#faf5ff";
    private static final String BRAND_BORDER  = "#e9d5ff";

    /*"https://TU-DOMINIO-PRODUCCION/images/logo1CB.webp";*/
    private static final String LOGO_URL =
            "https://wsrv.nl/?url=raw.githubusercontent.com/LeviTorresx/FrontEnd_BlaShape/master/public/images/logo1CB.webp&output=png&w=240";

    private final PqrsRepository pqrsRepository;
    private final CustomerRepository customerRepository;
    private final CarpenterRepository carpenterRepository;
    private final PqrsMapper pqrsMapper;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final WorkshopRepository workshopRepository;

    @Value("${BRAND_SUPPORT_EMAIL:soporte@blashape.com}")
    private String supportEmail;

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
                    .findFirstByRoleAndIsActiveTrueOrderByCarpenterIdAsc(UserRole.ADMIN)
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
    @Transactional
    public PqrsDTO getPqrs(Long id, String token) {
        Pqrs pqrs = pqrsRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "PQRS no encontrada con ID: " + id));

        if (token != null && !token.isBlank()) {
            Carpenter viewer = safeExtractCarpenter(token);
            if (viewer != null) tryMarkInProgress(pqrs, viewer);
        }

        return pqrsMapper.toDTO(pqrs);
    }

    /** Marca la PQRS como EN_PROCESO sólo si la abre el carpintero asignado. */
    private void tryMarkInProgress(Pqrs pqrs, Carpenter viewer) {
        boolean isAssignee = pqrs.getCarpenter() != null
                && pqrs.getCarpenter().getCarpenterId().equals(viewer.getCarpenterId());

        if (isAssignee && pqrs.getStatus() == PqrsStatus.PENDIENTE) {
            pqrs.setStatus(PqrsStatus.EN_PROCESO);
            pqrsRepository.save(pqrs);
        }
    }

    /** Extrae el carpintero del token sin lanzar si es inválido (la lectura no debe fallar por eso). */
    private Carpenter safeExtractCarpenter(String token) {
        try {
            return extractCarpenterFromToken(token);
        } catch (Exception ex) {
            return null;
        }
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
            String to = pqrs.getCustomer() != null
                    ? pqrs.getCustomer().getEmail()
                    : pqrs.getGuestEmail();
            if (to == null) return;

            String name        = esc(displayName(pqrs));
            String code        = esc(pqrs.getTrackingCode());
            String subject     = esc(pqrs.getSubject());
            String typeLabel   = esc(pqrs.getType().name());
            String scopeLabel  = pqrs.getScope() == PqrsScope.GENERAL
                    ? "Solicitud general (equipo BlaShape)"
                    : "Taller: " + (pqrs.getWorkshop() != null
                                    ? esc(pqrs.getWorkshop().getName()) : "—");


            String htmlBody = String.format("""
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;
                        background:#ffffff;border-radius:14px;overflow:hidden;
                        border:1px solid %5$s;">
              <div style="background:linear-gradient(135deg,%1$s,%2$s);
                          padding:24px 28px;">
                <img src="%9$s" alt="BlaShape" height="36"
                     style="display:block;border:0;height:36px;">
              </div>
              <div style="padding:28px;color:#333;">
                <h2 style="color:%1$s;margin:0 0 8px 0;">¡Hola %3$s!</h2>
                <p style="margin:0 0 18px 0;color:#555;line-height:1.5;">
                  Hemos recibido tu PQRS. Te notificaremos por correo cuando recibas
                  una respuesta y puedes consultar el estado en cualquier momento.
                </p>

                <div style="background:%4$s;border:1px solid %5$s;border-radius:10px;
                            padding:16px;text-align:center;margin:18px 0;">
                  <p style="margin:0 0 4px 0;font-size:11px;letter-spacing:.1em;
                            text-transform:uppercase;color:%1$s;font-weight:700;">
                    Código de radicado
                  </p>
                  <p style="margin:0;font-family:'Courier New',monospace;font-size:20px;
                            font-weight:700;color:%1$s;">%6$s</p>
                </div>

                <p style="margin:6px 0;"><strong>Asunto:</strong> %7$s</p>
                <p style="margin:6px 0;"><strong>Tipo:</strong> %8$s</p>
                <p style="margin:6px 0;"><strong>Destinatario:</strong> %10$s</p>

                <div style="text-align:center;margin:24px 0;">
                  <a href="%11$s"
                     style="background:%1$s;color:#fff;text-decoration:none;
                            padding:12px 26px;border-radius:8px;font-weight:600;
                            display:inline-block;">
                    Ver seguimiento →
                  </a>
                </div>

                <p style="font-size:12px;color:#888;margin:20px 0 0 0;">
                  ¿Dudas? Escríbenos a
                  <a href="mailto:%12$s" style="color:%1$s;">%12$s</a>.
                </p>
              </div>
              <div style="background:%4$s;padding:14px;text-align:center;
                          font-size:11px;color:#888;">
                © %13$d BlaShape · Correo automático, no respondas a este mensaje.
              </div>
            </div>
            """,
                    BRAND_PURPLE, BRAND_ACCENT, name, BRAND_BG, BRAND_BORDER,
                    code, subject, typeLabel, LOGO_URL, scopeLabel,
                    esc(link), esc(supportEmail), java.time.Year.now().getValue()
            );

            emailService.sendEmail(
                    to,
                    "Hemos recibido tu PQRS — Radicado " + pqrs.getTrackingCode(),
                    htmlBody
            );
        } catch (Exception ex) {
            // No queremos que un fallo de email tumbe la creación
            // (idealmente: log + métrica)
        }
    }

    private void notifyResponse(Pqrs pqrs) {
        try {
            String to = pqrs.getCustomer() != null
                    ? pqrs.getCustomer().getEmail()
                    : pqrs.getGuestEmail();
            if (to == null) return;

            String name     = esc(displayName(pqrs));
            String code     = esc(pqrs.getTrackingCode());
            String subject  = esc(pqrs.getSubject());
            String response = esc(pqrs.getResponse());
            String logoUrl  = trim(frontendUrl) + "/images/logo-email.png";

            String htmlBody = String.format("""
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;
                        background:#ffffff;border-radius:14px;overflow:hidden;
                        border:1px solid %5$s;">
              <div style="background:linear-gradient(135deg,%1$s,%2$s);
                          padding:24px 28px;">
                <img src="%6$s" alt="BlaShape" height="36"
                     style="display:block;border:0;height:36px;">
              </div>
              <div style="padding:28px;color:#333;">
                <h2 style="color:%1$s;margin:0 0 8px 0;">
                  ¡Tu PQRS tiene respuesta, %3$s!
                </h2>
                <p style="margin:0 0 18px 0;color:#555;line-height:1.5;">
                  Hemos atendido tu solicitud
                  <strong style="color:%1$s;">%7$s</strong>.
                </p>

                <div style="background:%4$s;border:1px solid %5$s;border-left:4px solid %1$s;
                            border-radius:10px;padding:16px 18px;margin:18px 0;">
                  <p style="margin:0 0 6px 0;font-size:11px;letter-spacing:.08em;
                            text-transform:uppercase;color:%1$s;font-weight:700;">
                    Respuesta del equipo
                  </p>
                  <p style="margin:0;color:#333;line-height:1.6;white-space:pre-wrap;">
                    %8$s
                  </p>
                </div>

                <p style="margin:6px 0;"><strong>Asunto:</strong> %9$s</p>

                <p style="font-size:12px;color:#888;margin:20px 0 0 0;">
                  Si tienes dudas adicionales, escríbenos a
                  <a href="mailto:%10$s" style="color:%1$s;">%10$s</a>.
                </p>
              </div>
              <div style="background:%4$s;padding:14px;text-align:center;
                          font-size:11px;color:#888;">
                © %11$d BlaShape · Correo automático, no respondas a este mensaje.
              </div>
            </div>
            """,
                    BRAND_PURPLE, BRAND_ACCENT, name, BRAND_BG, BRAND_BORDER,
                    logoUrl, code, response, subject, esc(supportEmail),
                    java.time.Year.now().getValue()
            );

            emailService.sendEmail(
                    to,
                    "Tu PQRS " + pqrs.getTrackingCode() + " tiene respuesta",
                    htmlBody
            );
        } catch (Exception ignored) { /* log */ }
    }

    private String displayName(Pqrs pqrs) {
        if (pqrs.getCustomer() != null && pqrs.getCustomer().getName() != null) {
            return pqrs.getCustomer().getName();
        }
        return (pqrs.getGuestName() != null && !pqrs.getGuestName().isBlank())
                ? pqrs.getGuestName() : "usuario";
    }

    private String trim(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Escapa HTML para evitar XSS en campos provenientes del usuario. */
    private String esc(String raw) {
        if (raw == null) return "";
        return raw.replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}