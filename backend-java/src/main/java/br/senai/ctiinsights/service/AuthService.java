package br.senai.ctiinsights.service;

import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** Conta administrativa configurada por ambiente; sessoes expiram e sao revogaveis. */
@Service
public class AuthService {
    private final String email;
    private final String passwordHash;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(@Value("${auth.admin-email:}") String email,
            @Value("${auth.admin-password:}") String password) {
        this.email = email.trim();
        if (!this.email.isBlank() && (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)) {
            throw new IllegalArgumentException("AUTH_ADMIN_PASSWORD deve ter ao menos 8 caracteres e no maximo 72 bytes UTF-8");
        }
        this.passwordHash = encoder.encode(this.email.isBlank() ? "" : password);
    }

    public Session login(String email, String password, boolean remember) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        boolean validPassword = encoder.matches(password, passwordHash);
        if (this.email.isBlank() || !this.email.equalsIgnoreCase(email.trim()) || !validPassword) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(Instant.now()));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(token, this.email, Instant.now().plusSeconds(remember ? 604800 : 28800));
        sessions.put(token, session);
        return session;
    }

    public Session find(String token) {
        if (token == null) return null;
        Session session = sessions.get(token);
        if (session != null && !session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    public record Session(String token, String email, Instant expiresAt) {
        @Override public String toString() {
            return "Session[token=[PROTEGIDO], email=" + email + ", expiresAt=" + expiresAt + "]";
        }
    }
}
