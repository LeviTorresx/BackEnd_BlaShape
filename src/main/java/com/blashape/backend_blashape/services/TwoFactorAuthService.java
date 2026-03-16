package com.blashape.backend_blashape.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorAuthService {

    private final EmailService emailService;

    // 15 minutos
    private static final long EXPIRATION_TIME = 15 * 60 * 1000;

    // reenvío permitido cada 30 segundos
    private static final long RESEND_DELAY = 30 * 1000;

    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

    public TwoFactorAuthService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendVerificationCode(String email) {

        long now = System.currentTimeMillis();

        VerificationCode existing = verificationCodes.get(email);

        // Evitar reenvíos demasiado rápidos
        if (existing != null && (now - existing.getCreatedAt()) < RESEND_DELAY) {
            throw new RuntimeException("Debes esperar antes de solicitar otro código.");
        }

        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        long expiresAt = now + EXPIRATION_TIME;

        VerificationCode verificationCode =
                new VerificationCode(code, expiresAt, now);

        verificationCodes.put(email, verificationCode);

        String subject = "Tu código de verificación";
        String body = "Tu código es: " + code +
                "\n\nEste código expira en 15 minutos.";

        emailService.sendEmail(email, subject, body);
    }

    public boolean verifyCode(String email, String code) {

        VerificationCode stored = verificationCodes.get(email);

        if (stored == null) {
            return false;
        }

        // Si expiró, eliminarlo
        if (stored.isExpired()) {
            verificationCodes.remove(email);
            return false;
        }

        if (!stored.getCode().equals(code)) {
            return false;
        }

        // Código correcto
        verifiedEmails.add(email);
        verificationCodes.remove(email);

        return true;
    }

    public boolean isVerified(String email) {
        return verifiedEmails.contains(email);
    }

    public void clearVerification(String email) {
        verifiedEmails.remove(email);
    }
}