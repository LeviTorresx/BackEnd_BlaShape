package com.blashape.backend_blashape.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String BASE_ENDPOINT = "/api_BS";

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors ->{})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(BASE_ENDPOINT + "/auth/login",
                                        BASE_ENDPOINT + "/auth/register",
                                        BASE_ENDPOINT + "/auth/verify-email",
                                        BASE_ENDPOINT + "/auth/resend-verification",
                                        BASE_ENDPOINT + "/auth/forgot-password",
                                        BASE_ENDPOINT + "/auth/reset-password",
                                        BASE_ENDPOINT + "/monetization/**",
                                        BASE_ENDPOINT + "/auth/verify-reset-code",
                                        BASE_ENDPOINT + "/stripe/**")
                                        .permitAll()
                        .requestMatchers(BASE_ENDPOINT + "/auth/**")
                                        .authenticated()
                        .requestMatchers(BASE_ENDPOINT + "/alert/create",
                                        BASE_ENDPOINT + "/alert/get/**",
                                        BASE_ENDPOINT + "/alert/edit/**",
                                        BASE_ENDPOINT + "/alert/delete/**",
                                        BASE_ENDPOINT + "/customer/**",
                                        BASE_ENDPOINT + "/cutting/**",
                                        BASE_ENDPOINT + "/furniture/**",
                                        BASE_ENDPOINT + "/workshop/**")
                                        .authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
