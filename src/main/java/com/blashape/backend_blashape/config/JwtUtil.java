package com.blashape.backend_blashape.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET}")
    private String SECRET_KEY;

    @Value("${JWT_EXPIRATION}")
    private long EXPIRATION_TIME; //ahora es long, no String

    @Value("${PQRS_TRACKING_EXPIRATION:2592000000}") // 30 días por defecto
    private long PQRS_TRACKING_EXPIRATION;

    private static final String PQRS_SCOPE = "pqrs-tracking";

    public String generateToken(String email, Long carpenterId, String plan) {
        return Jwts.builder()
                .setSubject(email)
                .claim("carpenterId", carpenterId)
                .claim("plan", plan)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractId(String token) {
        return extractAllClaims(token).get("carpenterId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new RuntimeException("Token no encontrado en las cookies");
    }

    public String extractPlan(String token) {
        return extractAllClaims(token).get("plan", String.class);
    }

    public String generatePqrsTrackingToken(Long pqrsId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("pqrsId", pqrsId)
                .claim("scope", PQRS_SCOPE)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + PQRS_TRACKING_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public Long extractPqrsIdFromTrackingToken(String token) {
        Claims claims = extractAllClaims(token);
        Object scope = claims.get("scope");
        if (!PQRS_SCOPE.equals(scope)) {
            throw new JwtException("Token con scope inválido para seguimiento de PQRS");
        }
        return claims.get("pqrsId", Long.class);
    }
}
