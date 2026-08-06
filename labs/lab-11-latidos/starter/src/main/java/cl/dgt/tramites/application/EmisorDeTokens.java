package cl.dgt.tramites.application;

import cl.dgt.tramites.web.dto.TokenDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Emite el JWT del login. Primero AUTENTICA (contra {@link DgtUserDetailsService} + BCrypt); si
 * las credenciales fallan, {@code authenticate} lanza {@code BadCredentialsException} y aquí no
 * se emite nada. Solo con la identidad probada se firma el token.
 *
 * <p>El token lleva: el {@code sub} (rut), la vigencia ({@code iat}/{@code exp}), y el claim
 * {@code roles}. Se firma con HMAC-SHA256 (simétrica): el mismo secreto firma y valida. La firma
 * es lo que convierte el token en una credencial y no en una opinión.
 */
@Service
public class EmisorDeTokens {

    /** Vigencia del token. Corta a propósito: un token robado caduca pronto (criterio de M9). */
    private static final Duration VIGENCIA = Duration.ofHours(1);

    private final AuthenticationManager autenticador;
    private final JwtEncoder encoder;

    public EmisorDeTokens(AuthenticationManager autenticador, JwtEncoder encoder) {
        this.autenticador = autenticador;
        this.encoder = encoder;
    }

    public TokenDto emitir(String rut, String clavePlana) {
        Authentication auth = autenticador.authenticate(
                new UsernamePasswordAuthenticationToken(rut, clavePlana));

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();

        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("dgt")
                .subject(rut)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenDto(token, "Bearer", VIGENCIA.toSeconds());
    }
}
