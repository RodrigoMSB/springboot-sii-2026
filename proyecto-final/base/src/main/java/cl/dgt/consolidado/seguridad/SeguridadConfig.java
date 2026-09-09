package cl.dgt.consolidado.seguridad;

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
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SeguridadConfig {

    @Value("${dgt.jwt.secreto}")
    private String secreto;

    @Bean
    SecurityFilterChain cadena(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        // La documentación de la API es pública: describe QUÉ endpoints hay, no
                        // devuelve ni un dato. Y sin esto `/swagger-ui.html` daría 401, que es la
                        // primera pantalla que alguien abre para entender el servicio.
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Las reglas del encargo van AQUÍ, y el sitio importa: esta lista se
                        // evalúa de arriba abajo y GANA LA PRIMERA REGLA QUE CASA.
                        // `anyRequest()` casa con todo, así que va la última — siempre.
                        //
                        // Ponerla DESPUÉS de `anyRequest()` es ruidoso, y eso es una suerte: la
                        // aplicación no arranca y dice por qué —«Can't configure requestMatchers
                        // after anyRequest»—, así que lo ves en el primer intento.
                        //
                        // Lo SILENCIOSO es ensombrecer tu regla con otra anterior que ya case
                        // con la misma ruta (un `.authenticated()` de más sobre
                        // `/consolidados/**`, por ejemplo): la segunda no se ejecuta jamás, no
                        // hay error ni WARNING, y cualquier autenticado —también el
                        // CONTRIBUYENTE— entra con un 200. Una sola regla por ruta.
                        //
                        // Y NO la escribas como `@PreAuthorize` en el controller: sin
                        // `@EnableMethodSecurity`, que este proyecto no trae, esa anotación se
                        // ignora ENTERA, sin error ni aviso, y el endpoint queda abierto a
                        // cualquier autenticado. Comprobado. La autorización vive en esta
                        // cadena y en ningún otro sitio — es lo que mira el criterio 4.
                        //
                        // La autoridad que trae el token es `ROLE_FISCALIZADOR`, tal cual: la
                        // arma `ServicioDeTokens` y la lee `conversorDeRoles`, ahí abajo, sin
                        // añadirle prefijo.
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
    }

    // Argon2id, lo que recomienda OWASP y lo mismo que enseña el Lab 09: lento en tiempo Y en
    // memoria, que es lo que deja fuera a las tarjetas gráficas. Los parámetros salen de la
    // fábrica de Spring Security — elegirlos a mano y mal deja Argon2 peor que BCrypt.
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

    @Bean
    JwtEncoder codificadorDeTokens() {
        return NimbusJwtEncoder.withSecretKey(clave()).build();
    }

    @Bean
    JwtDecoder decodificadorDeTokens() {
        return NimbusJwtDecoder.withSecretKey(clave()).build();
    }

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
