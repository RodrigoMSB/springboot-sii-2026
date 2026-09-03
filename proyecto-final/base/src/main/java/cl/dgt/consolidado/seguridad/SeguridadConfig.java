package cl.dgt.consolidado.seguridad;

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
                        // Las reglas del encargo van aquí.
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
