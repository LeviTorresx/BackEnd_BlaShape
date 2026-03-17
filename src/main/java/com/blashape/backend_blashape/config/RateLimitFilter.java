package com.blashape.backend_blashape.config;

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (path.contains("/auth/register") || path.contains("/auth/verify-email")) {

            String ip = request.getRemoteAddr();

            Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

            if (!bucket.tryConsume(1)) {

                httpResponse.setStatus(429);
                httpResponse.getWriter().write("Demasiados intentos. Intenta más tarde.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}