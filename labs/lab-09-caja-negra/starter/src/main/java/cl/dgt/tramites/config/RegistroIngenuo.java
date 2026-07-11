package cl.dgt.tramites.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * El "log" del practicante: una línea de texto plano por petición, SIN traceId. Con 30 peticiones
 * concurrentes, es un muro entrelazado imposible de seguir. Este componente se BORRA en la solución
 * y se reemplaza por el filtro de traza (MDC) + el aspecto de auditoría (JSON).
 */
@Component
public class RegistroIngenuo extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("REGISTRO");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        // Sin traceId, sin correlación: buena suerte reconstruyendo una operación entre mil líneas.
        log.info("peticion {} {}", req.getMethod(), req.getRequestURI());
        chain.doFilter(req, res);
    }
}
