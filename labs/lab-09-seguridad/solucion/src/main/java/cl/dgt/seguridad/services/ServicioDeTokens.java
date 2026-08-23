package cl.dgt.seguridad.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class ServicioDeTokens {

    private static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final JwtEncoder codificador;

    public ServicioDeTokens(JwtEncoder codificador) {
        this.codificador = codificador;
    }

    public String emitir(Authentication autenticacion) {
        Instant ahora = Instant.now();

        // Sólo los roles: Spring Security 7 añade además autoridades técnicas (FACTOR_PASSWORD)
        // que no son del negocio y ensuciarían el token que se lee en clase.
        String roles = autenticacion.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.joining(" "));

        JwtClaimsSet cuerpo = JwtClaimsSet.builder()
                .issuer("lab08")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .subject(autenticacion.getName())
                .claim("scope", roles)
                .build();

        return codificador.encode(JwtEncoderParameters.from(cuerpo)).getTokenValue();
    }
}
