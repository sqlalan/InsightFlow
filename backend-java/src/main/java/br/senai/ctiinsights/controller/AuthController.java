package br.senai.ctiinsights.controller;

import br.senai.ctiinsights.config.SecurityConfig;
import br.senai.ctiinsights.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public ResponseEntity<AuthService.Session> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(auth.login(request.email(), request.password(), request.remember()));
    }

    /** Pedido de conta: responde 201, mas so permite entrar depois que o admin aprovar. */
    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, String>> cadastrar(@Valid @RequestBody CadastroRequest request) {
        auth.cadastrar(request.nome(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Pedido enviado. Você poderá entrar assim que o administrador aprovar sua conta."));
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) { return Map.of("email", principal.getName()); }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        auth.logout(SecurityConfig.token(request));
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(@NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 72) String password, boolean remember) {
        @Override public String toString() {
            return "LoginRequest[email=" + email + ", password=[PROTEGIDO], remember=" + remember + "]";
        }
    }

    public record CadastroRequest(@NotBlank(message = "informe seu nome") @Size(max = 120) String nome,
            @NotBlank(message = "informe seu e-mail") @Email(message = "e-mail em formato inválido")
            @Size(max = 255) String email,
            @NotBlank @Size(min = 8, max = 72, message = "a senha deve ter de 8 a 72 caracteres") String password) {
        @Override public String toString() {
            return "CadastroRequest[nome=" + nome + ", email=" + email + ", password=[PROTEGIDO]]";
        }
    }
}
