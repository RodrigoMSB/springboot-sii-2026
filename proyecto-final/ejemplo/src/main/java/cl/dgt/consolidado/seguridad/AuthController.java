package cl.dgt.consolidado.seguridad;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager gestor;
    private final ServicioDeTokens tokens;

    public AuthController(AuthenticationManager gestor, ServicioDeTokens tokens) {
        this.gestor = gestor;
        this.tokens = tokens;
    }

    public record Credenciales(String usuario, String clave) {
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Credenciales credenciales) {
        try {
            Authentication autenticado = gestor.authenticate(
                    new UsernamePasswordAuthenticationToken(credenciales.usuario(), credenciales.clave()));
            return ResponseEntity.ok(Map.of("token", tokens.emitir(autenticado)));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }
}
