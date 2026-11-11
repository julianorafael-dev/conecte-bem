package com.conectebem.backend.config;

import com.conectebem.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sem autenticação (ou token inválido/expirado) -> 401; autenticado sem papel -> 403.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"erro\":\"Unauthorized\",\"mensagem\":\"Entre na sua conta para continuar.\"}");
                        })
                        // Não usar sendError: o encaminhamento para /error pode resultar em 401.
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"erro\":\"Forbidden\",\"mensagem\":\"Você não tem permissão para realizar esta ação.\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/auth/**").permitAll() // Liberta as rotas de registo e login
                        .requestMatchers(HttpMethod.POST, "/contato").permitAll() // Formulário de contato, aberto a visitantes
                        .requestMatchers("/ongs/me/**").hasRole("ONG")
                        .requestMatchers("/voluntarios/me").hasRole("VOLUNTARIO")
                        // Rotas de inscrições: regras específicas precisam vir antes
                        // do GET público de /oportunidades/** abaixo.
                        .requestMatchers(HttpMethod.GET, "/oportunidades/*/inscricoes").hasRole("ONG")
                        .requestMatchers(HttpMethod.POST, "/inscricoes").hasRole("VOLUNTARIO")
                        .requestMatchers(HttpMethod.GET, "/inscricoes/me").hasRole("VOLUNTARIO")
                        .requestMatchers(HttpMethod.DELETE, "/inscricoes/**").hasRole("VOLUNTARIO")
                        .requestMatchers(HttpMethod.PUT, "/inscricoes/*/confirmar", "/inscricoes/*/recusar").hasRole("ONG")
                        .requestMatchers(HttpMethod.GET, "/ongs/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/ongs/**").hasRole("ONG")
                        .requestMatchers(HttpMethod.GET, "/oportunidades/**").permitAll()
                        // Defesa em profundidade (alinhado com a Pessoa 2): somente
                        // contas do tipo ONG gerenciam oportunidades. Esses matchers
                        // precisam vir APÓS o GET público acima e ANTES do anyRequest().
                        .requestMatchers(HttpMethod.POST, "/oportunidades/**").hasRole("ONG")
                        .requestMatchers(HttpMethod.PUT, "/oportunidades/**").hasRole("ONG")
                        .requestMatchers(HttpMethod.DELETE, "/oportunidades/**").hasRole("ONG")
                        .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                        // Categorias globais são mantidas pelo banco até existir um perfil administrador.
                        .requestMatchers("/categorias", "/categorias/**").denyAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Criptografia segura exigida na entrega
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
