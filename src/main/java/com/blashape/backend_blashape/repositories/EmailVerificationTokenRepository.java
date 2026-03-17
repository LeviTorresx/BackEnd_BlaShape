package com.blashape.backend_blashape.repositories;

import com.blashape.backend_blashape.entitys.Carpenter;
import com.blashape.backend_blashape.entitys.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    List<EmailVerificationToken> findByCarpenter(Carpenter carpenter);
}