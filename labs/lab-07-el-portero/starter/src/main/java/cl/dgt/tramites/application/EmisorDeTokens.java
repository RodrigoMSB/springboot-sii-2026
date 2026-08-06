package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.UsuarioRepository;
import cl.dgt.tramites.web.dto.TokenDto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * "Login" del practicante. Devuelve {@code base64(rut:rol)} — y ni siquiera mira la clave.
 * TODO_2: aquí debe ir el login real (contra la tabla usuario, BCrypt) que emite un JWT FIRMADO.
 */
@Service
public class EmisorDeTokens {

    private final UsuarioRepository usuarios;

    public EmisorDeTokens(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public TokenDto emitir(String rut, String clavePlana) {
        // La clave se ignora: ese es el crimen. Se mira solo el rol, para ponerlo en el "token".
        String rol = usuarios.findByRut(rut).map(u -> u.getRol().name()).orElse("DESCONOCIDO");
        String token = Base64.getEncoder()
                .encodeToString((rut + ":" + rol).getBytes(StandardCharsets.UTF_8));
        return new TokenDto(token, "Bearer", 3600);
    }
}
