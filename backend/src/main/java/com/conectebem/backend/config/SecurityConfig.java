package com.conectebem.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        // TEMPORÁRIO: liberado porque a autenticação JWT (Pessoa 2)
                        // ainda não existe. Sem isso, TODO endpoint (inclusive os GETs
                        // públicos de listagem) responde 401, pois o Spring Security
                        // exige login e não há nenhum jeito de logar ainda.
                        // Quando o módulo de auth estiver pronto:
                        //   - manter GET /categorias/**, /ongs/**, /oportunidades/** públicos
                        //   - exigir token + role ONG em POST/PUT/DELETE de /oportunidades/**
                        //     e /ongs/** (ver TODOs nos controllers correspondentes)
                        .requestMatchers("/categorias/**", "/ongs/**", "/oportunidades/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
