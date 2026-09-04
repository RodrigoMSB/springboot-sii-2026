package cl.dgt.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SeguridadConfig {

    @Value("${microservicios.jwt.secreto}")
    private String secreto;

    @Bean
    SecurityFilterChain cadena(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                // TODO paso 6: hoy la puerta está abierta de par en par.
                .authorizeHttpRequests(rutas -> rutas.anyRequest().permitAll())
                .build();
    }

    @Bean
    UserDetailsService usuarios(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
                User.withUsername("carolina").password(codificador.encode("dgt2026"))
                        .roles("FUNCIONARIO").build(),
                User.withUsername("jefatura").password(codificador.encode("dgt2026"))
                        .roles("ADMIN").build());
    }

    // El mismo codificador del Lab 09 y del proyecto final: Argon2id, con los parámetros de la
    // fábrica de Spring Security. El arco entero usa uno solo.
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
}
