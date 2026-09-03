package cl.dgt.seguridad.services;

import org.springframework.beans.factory.annotation.Value;
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

    // Media hora por defecto. El instructor la baja a 40 segundos desde el yml para
    // enseñar el token vencido, sin tocar una línea de código.
    private final Duration vigencia;

    private final JwtEncoder codificador;

    public ServicioDeTokens(JwtEncoder codificador,
                            @Value("${lab09.jwt.vigencia-segundos:1800}") long vigenciaSegundos) {
        this.codificador = codificador;
        this.vigencia = Duration.ofSeconds(vigenciaSegundos);
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
                .issuer("lab09")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(vigencia))
                .subject(autenticacion.getName())
                .claim("scope", roles)
                .build();

        return codificador.encode(JwtEncoderParameters.from(cuerpo)).getTokenValue();
    }
}
