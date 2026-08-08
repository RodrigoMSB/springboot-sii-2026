package cl.dgt.tramites.config.observabilidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * El hilo de Ariadna. En cada petición pone un {@code traceId} en el MDC (Mapped Diagnostic
 * Context) de SLF4J: un identificador único que TODA línea de log de esa petición llevará. Filtrar
 * el log por un {@code traceId} reconstruye una operación completa, aislada del resto del muro.
 *
 * <p>El MDC es un mapa por-HILO. Por eso hay que LIMPIARLO al final ({@code finally}): el hilo
 * vuelve al pool y atenderá otra petición; un {@code traceId} olvidado contaminaría la siguiente.
 *
 * <p>Si el cliente ya trae un {@code X-Trace-Id} (viene de otro servicio), se respeta —así la
 * traza cruza fronteras—; si no, se genera. Y se devuelve en la respuesta, para que quien reporte
 * un problema pueda decir "búscalo por este id".
 */
@Component
@Order(1)
public class FiltroDeTraza extends OncePerRequestFilter {

    public static final String CLAVE_MDC = "traceId";
    public static final String CABECERA = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String traceId = req.getHeader(CABECERA);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(CLAVE_MDC, traceId);
        res.setHeader(CABECERA, traceId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(CLAVE_MDC);   // el hilo vuelve al pool limpio
        }
    }
}
