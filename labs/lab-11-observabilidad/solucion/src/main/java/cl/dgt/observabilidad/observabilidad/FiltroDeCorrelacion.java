package cl.dgt.observabilidad.observabilidad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Un id por petición, puesto en el MDC: a partir de aquí TODA línea de log lo lleva,
// sin que nadie lo pase por parámetro.
@Component
public class FiltroDeCorrelacion extends OncePerRequestFilter {

    public static final String CABECERA = "X-Trace-Id";
    public static final String CLAVE = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        // Si el que llama ya trae uno, se respeta: así el id cruza de un servicio a otro.
        String id = peticion.getHeader(CABECERA);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(CLAVE, id);
        respuesta.setHeader(CABECERA, id);
        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            // Imprescindible: el hilo se reutiliza para la petición siguiente.
            MDC.remove(CLAVE);
        }
    }
}
