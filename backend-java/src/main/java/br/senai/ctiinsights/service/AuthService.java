package br.senai.ctiinsights.service;

import br.senai.ctiinsights.domain.PerfilUsuario;
import br.senai.ctiinsights.domain.StatusUsuario;
import br.senai.ctiinsights.domain.Usuario;
import br.senai.ctiinsights.exception.ContaNaoLiberadaException;
import br.senai.ctiinsights.exception.EmailJaCadastradoException;
import br.senai.ctiinsights.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
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

/**
 * Cadastro, login e sessoes.
 *
 * As contas ficam na tabela usuario. A conta do administrador vem das variaveis
 * AUTH_ADMIN_EMAIL e AUTH_ADMIN_PASSWORD e e criada ao iniciar a API: e ela que
 * aprova as outras. Sessoes ficam em memoria, expiram e sao revogaveis.
 */
@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final String adminEmail;
    private final String adminPassword;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    /** Usado quando o e-mail nao existe: o login demora o mesmo tempo e nao entrega quem tem conta. */
    private final String hashFalso = encoder.encode("conta-inexistente");

    public AuthService(UsuarioRepository usuarios, @Value("${auth.admin-email:}") String email,
            @Value("${auth.admin-password:}") String password) {
        this.usuarios = usuarios;
        this.adminEmail = email.trim().toLowerCase();
        if (!this.adminEmail.isBlank() && (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)) {
            throw new IllegalArgumentException("AUTH_ADMIN_PASSWORD deve ter ao menos 8 caracteres e no maximo 72 bytes UTF-8");
        }
        this.adminPassword = password;
    }

    /** Garante que a conta do admin exista e siga as variaveis de ambiente, mesmo se a senha mudou. */
    @PostConstruct
    void criarAdmin() {
        if (adminEmail.isBlank()) return;
        Usuario admin = usuarios.findByEmail(adminEmail).orElseGet(() ->
                new Usuario("Administrador", adminEmail, "", PerfilUsuario.ADMIN, StatusUsuario.ATIVO));
        admin.setSenhaHash(encoder.encode(adminPassword));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setStatus(StatusUsuario.ATIVO);
        usuarios.save(admin);
    }

    /** Conta nova sempre nasce CONSULTOR e PENDENTE: so entra depois que o admin aprovar. */
    public Usuario cadastrar(String nome, String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha pode ter no máximo 72 bytes.");
        }
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarios.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }
        return usuarios.save(new Usuario(nome.trim(), emailNormalizado, encoder.encode(password),
                PerfilUsuario.CONSULTOR, StatusUsuario.PENDENTE));
    }

    public Session login(String email, String password, boolean remember) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        Usuario usuario = usuarios.findByEmail(email.trim().toLowerCase()).orElse(null);
        boolean validPassword = encoder.matches(password, usuario != null ? usuario.getSenhaHash() : hashFalso);
        if (usuario == null || !validPassword) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }
        // So depois de a senha conferir dizemos por que a conta nao entra.
        if (usuario.getStatus() == StatusUsuario.PENDENTE) {
            throw new ContaNaoLiberadaException("CONTA_PENDENTE",
                    "Sua conta ainda aguarda aprovação do administrador.");
        }
        if (usuario.getStatus() == StatusUsuario.BLOQUEADO) {
            throw new ContaNaoLiberadaException("CONTA_BLOQUEADA",
                    "Sua conta está bloqueada. Fale com o administrador.");
        }
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(Instant.now()));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(token, usuario.getEmail(), usuario.getNome(), usuario.getPerfil().name(),
                Instant.now().plusSeconds(remember ? 604800 : 28800));
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

    /** Usado ao bloquear uma conta: quem estava logado sai na proxima requisicao. */
    public void encerrarSessoes(String email) {
        sessions.values().removeIf(session -> session.email().equals(email));
    }

    public record Session(String token, String email, String nome, String perfil, Instant expiresAt) {
        @Override public String toString() {
            return "Session[token=[PROTEGIDO], email=" + email + ", perfil=" + perfil + ", expiresAt=" + expiresAt + "]";
        }
    }
}
