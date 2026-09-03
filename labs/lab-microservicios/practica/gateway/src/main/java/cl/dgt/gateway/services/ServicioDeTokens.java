package cl.dgt.gateway.services;

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

    private static final Duration VIGENCIA = Duration.ofHours(3);

    private final JwtEncoder codificador;

    public ServicioDeTokens(JwtEncoder codificador) {
        this.codificador = codificador;
    }

    public String emitir(Authentication autenticacion) {
        Instant ahora = Instant.now();
        String roles = autenticacion.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.joining(" "));

        JwtClaimsSet cuerpo = JwtClaimsSet.builder()
                .issuer("microservicios-gateway")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .subject(autenticacion.getName())
                .claim("scope", roles)
                .build();

        return codificador.encode(JwtEncoderParameters.from(cuerpo)).getTokenValue();
    }
}
