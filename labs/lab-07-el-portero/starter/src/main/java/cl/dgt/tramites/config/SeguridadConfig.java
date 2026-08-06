package cl.dgt.tramites.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * "Seguridad" del practicante. Parece un portero. Es un disfraz.
 *
 * <p>Deja pasar TODO ({@code permitAll}) y trae un filtro que lee un "token" que es
 * {@code base64(rut:rol)} y te cree lo que diga. Codificar no es cifrar, y cifrar no es firmar:
 * cualquiera fabrica su token con {@code echo 'x:FUNCIONARIO' | base64}. Este es el crimen que
 * el laboratorio desarma. (TODO_1..TODO_4.)
 */
@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // TODO_1: aquí NO se deniega por defecto. Todo está abierto: un curl anónimo emite folios.
            .authorizeHttpRequests(reglas -> reglas.anyRequest().permitAll())
            .addFilterBefore(new FiltroBase64Falso(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** TODO_3: este "validador" no valida nada. Decodifica base64 y cree. No hay firma que romper. */
    static class FiltroBase64Falso extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String plano = new String(Base64.getDecoder().decode(header.substring(7)),
                            StandardCharsets.UTF_8);
                    String[] partes = plano.split(":");   // rut:rol — sin firma, sin validación
                    if (partes.length == 2) {
                        var auth = new UsernamePasswordAuthenticationToken(partes[0], null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + partes[1])));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (IllegalArgumentException ignorado) {
                    // un base64 malo: lo ignoramos y seguimos (permitAll igual deja pasar)
                }
            }
            chain.doFilter(req, res);
        }
    }
}
