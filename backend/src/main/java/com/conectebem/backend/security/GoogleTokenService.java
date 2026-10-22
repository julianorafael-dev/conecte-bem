package com.conectebem.backend.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleTokenService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenService(@Value("${google.client.id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public GoogleIdToken.Payload verificar(String tokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(tokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Token do Google inválido ou expirado.");
            }
            return idToken.getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao validar token do Google: " + e.getMessage());
        }
    }
}
