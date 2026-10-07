package com.conectebem.backend.controller;

import com.conectebem.backend.dto.LoginRequestDTO;
import com.conectebem.backend.dto.RegistroRequestDTO;
import com.conectebem.backend.dto.TokenResponseDTO;
import com.conectebem.backend.dto.UsuarioResponseDTO;
import com.conectebem.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        UsuarioResponseDTO usuarioCriado = authService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        TokenResponseDTO token = authService.login(dto);
        return ResponseEntity.ok(token);
    }
}