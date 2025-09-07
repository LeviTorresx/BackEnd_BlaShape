package com.blashape.backend_blashape.services;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorAuthService {

    private final EmailService emailService;

    // Guarda temporalmente los códigos enviados
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // Guarda qué usuarios ya pasaron la verificación
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet();

    public TwoFactorAuthService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendVerificationCode(String email) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6 dígitos
        verificationCodes.put(email, code);

        String subject = "Tu código de verificación";
        String body = "Tu código es: " + code;

        emailService.sendEmail(email, subject, body);
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        if (storedCode != null && storedCode.equals(code)) {
            verifiedEmails.add(email); // ✅ marcamos como verificado
            verificationCodes.remove(email); // eliminamos el código (opcional)
            return true;
        }
        return false;
    }

    public boolean isVerified(String email) {
        return verifiedEmails.contains(email);
    }

    public void clearVerification(String email) {
        verifiedEmails.remove(email);
    }
}


