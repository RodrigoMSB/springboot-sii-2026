package cl.dgt.seguridad.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SeguridadConfig {

    // Clave simétrica: la misma firma y verifica. En producción va fuera del código.
    @Value("${lab08.jwt.secreto}")
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
                        .requestMatchers("/productos/administracion").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    PasswordEncoder codificadorDeClaves() {
        return new BCryptPasswordEncoder();
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
        return NimbusJwtDecoder.withSecretKey(clave()).build();
    }

    // Sin esto los roles llegarían como `SCOPE_ROLE_ADMIN`; con esto, como `ROLE_ADMIN`.
    @Bean
    JwtAuthenticationConverter conversorDeRoles() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthorityPrefix("");
        autoridades.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(autoridades);
        return conversor;
    }
}
