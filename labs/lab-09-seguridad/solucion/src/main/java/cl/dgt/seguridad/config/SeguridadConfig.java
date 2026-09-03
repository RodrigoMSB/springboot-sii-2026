package cl.dgt.seguridad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class SeguridadConfig {

    // Clave simétrica: la misma firma y verifica. En producción va fuera del código.
    @Value("${lab09.jwt.secreto}")
    private String secreto;

    @Bean
    SecurityFilterChain cadena(HttpSecurity http) throws Exception {
        return http
                // Sin sesión: cada petición trae su token y se basta sola.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF protege sesiones con cookie; aquí no hay ninguna.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/auth/login").permitAll()
                        // El conversor por defecto lee el claim `scope` y le antepone `SCOPE_`:
                        // el token dice ROLE_ADMIN y la autoridad se llama SCOPE_ROLE_ADMIN.
                        .requestMatchers("/productos/administracion").hasAuthority("SCOPE_ROLE_ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    // Argon2id, que es lo que recomienda OWASP hoy. Los parámetros salen de la fábrica de
    // Spring Security en vez de escribirse a mano: elegirlos mal deja el hash peor que BCrypt.
    @Bean
    PasswordEncoder codificadorDeClaves() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    AuthenticationManager gestorDeAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    private SecretKeySpec clave() {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // `withSecretKey` fija HS256 solo. Con el constructor genérico habría que declarar el
    // algoritmo a mano o falla con «Failed to select a JWK signing key».
    @Bean
    JwtEncoder codificadorDeTokens() {
        return NimbusJwtEncoder.withSecretKey(clave()).build();
    }

    @Bean
    JwtDecoder decodificadorDeTokens() {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(clave()).build();

        // Tolerancia de reloj a CERO, sólo para el laboratorio: de fábrica son 60 segundos, y un
        // token de 40 viviría 100 — la demo del paso 4 no cuadraría con lo que dice el yml.
        // En producción esta tolerancia se deja: los relojes de dos servidores nunca coinciden al
        // segundo, y rechazar un token por medio segundo de deriva es peor que aceptarlo por medio
        // minuto.
        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO)));
        return decodificador;
    }
}
