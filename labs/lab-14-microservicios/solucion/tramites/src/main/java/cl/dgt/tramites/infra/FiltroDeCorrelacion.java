package cl.dgt.tramites.infra;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// El mismo filtro del Lab 11, ahora en CUATRO procesos. Si el que llama trae el id,
// se respeta; ahí es donde el id cruza de un servicio al siguiente.
@Component
public class FiltroDeCorrelacion extends OncePerRequestFilter {

    public static final String CABECERA = "X-Trace-Id";
    public static final String CLAVE = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String id = peticion.getHeader(CABECERA);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(CLAVE, id);
        respuesta.setHeader(CABECERA, id);
        try {
            cadena.doFilter(peticion, respuesta);
        } finally {
            MDC.remove(CLAVE);
        }
    }
}
