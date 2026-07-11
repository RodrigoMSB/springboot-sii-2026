package cl.dgt.tramites.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * El portero. Cierra la puerta por defecto y define quién pasa.
 *
 * <p><strong>Denegar por defecto</strong> ({@code anyRequest().authenticated()}): una ruta nueva
 * nace CERRADA. La lista blanca es de puertas <em>abiertas</em> —health, login, la doc—, nunca de
 * cerradas: olvidar una regla deja la puerta con llave, no abierta de par en par. El error seguro.
 *
 * <p>El token se VALIDA con un Resource Server (OAuth2): {@code JwtDecoder} comprueba la firma
 * HMAC antes de dejar ver el contenido. Un token adulterado no pasa, aunque su payload sea
 * perfecto: sin la firma correcta, es una opinión.
 *
 * <p>CSRF va deshabilitado a propósito: esta API es sin estado (sin cookie de sesión; la
 * credencial viaja en el header {@code Authorization}), así que no hay sesión que un tercero
 * pueda montar. CSRF, CORS y las cabeceras de seguridad son la materia del Lab 08.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // habilita @PreAuthorize (TODO_4)
public class SeguridadConfig {

    /**
     * El secreto de firma (HMAC). Llega por {@code dgt.jwt.secret}, que en dev tiene un default de
     * utilería y en prod EXIGE la variable de entorno {@code DGT_JWT_SECRET} (ver
     * {@link VerificadorDeSecretosProd}). Misma doctrina que el Lab 01: ninguna clave real en el repo.
     */
    private final SecretKey clave;

    public SeguridadConfig(@Value("${dgt.jwt.secret}") String secreto) {
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // TODO_4 · CORS explícito (el portero de los navegadores) y cabeceras de endurecimiento.
            .cors(Customizer.withDefaults())
            .headers(h -> h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(reglas -> reglas
                // --- La lista blanca: SOLO puertas abiertas ---
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // --- Todo lo demás: cerrado. Deny by default. ---
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeRoles())));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // el mismo cost con que nació la semilla
    }

    /**
     * CORS: qué ORÍGENES (dominios de navegador) pueden llamar a esta API. Nominal, NO {@code *}:
     * poner {@code *} es rendirse —le dice al navegador "que llame cualquiera"—. Solo el front de
     * Mi DGT. Un preflight desde otro origen no recibe permiso.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("https://mi.dgt.cl"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", cfg);
        return fuente;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /** Valida la firma del JWT: si un byte del payload cambió, la firma no cuadra y el token muere. */
    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Firma el JWT en el login. Mismo secreto: simétrica (HS256). */
    @Bean
    JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> jwks = new ImmutableSecret<>(clave);
        return new NimbusJwtEncoder(jwks);
    }

    /** El claim {@code roles} del token se convierte en autoridades {@code ROLE_<rol>}. */
    private JwtAuthenticationConverter conversorDeRoles() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("roles");
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(autoridades);
        return conv;
    }
}
