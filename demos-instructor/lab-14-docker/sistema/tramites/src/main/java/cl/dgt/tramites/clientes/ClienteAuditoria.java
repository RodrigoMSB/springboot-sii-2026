package cl.dgt.tramites.clientes;

import cl.dgt.tramites.entities.Tramite;
import cl.dgt.tramites.infra.FiltroDeCorrelacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

// El aviso a auditoría NO va dentro de la petición del usuario: se manda y no se espera.
// Ahí está la consistencia eventual del paso 8, y también su precio.
@Component
public class ClienteAuditoria {

    private static final Logger log = LoggerFactory.getLogger(ClienteAuditoria.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final RestClient http;

    public ClienteAuditoria(@Value("${lab14.auditoria.url}") String url) {
        JdkClientHttpRequestFactory fabrica =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        fabrica.setReadTimeout(TIMEOUT);
        this.http = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    public void avisarDeUnTramiteNuevo(Tramite tramite) {
        // El id de correlación vive en el MDC del hilo de la petición: hay que copiarlo
        // ANTES de saltar a otro hilo, porque allá el MDC está vacío.
        String traceId = MDC.get(FiltroDeCorrelacion.CLAVE);

        Thread.ofVirtual().name("aviso-auditoria").start(() -> {
            MDC.put(FiltroDeCorrelacion.CLAVE, traceId);
            try {
                http.post()
                        .uri("/auditoria/eventos")
                        .header(FiltroDeCorrelacion.CABECERA, traceId)
                        .body(Map.of("evento", "TRAMITE_CREADO",
                                "tramiteId", tramite.getId(),
                                "rutContribuyente", tramite.getRutContribuyente()))
                        .retrieve()
                        .toBodilessEntity();
                log.info("[TRAMITES] auditoría acusó recibo del trámite {}", tramite.getId());
            } catch (Exception e) {
                // Auditoría caída NO deshace el trámite. Es una elección, y tiene su precio:
                // ese evento se perdió. Quien lo quiera garantizado necesita una cola.
                log.warn("[TRAMITES] auditoría no recibió el aviso del trámite {}: {}. "
                        + "El trámite queda creado igual.", tramite.getId(), e.getClass().getSimpleName());
            } finally {
                MDC.remove(FiltroDeCorrelacion.CLAVE);
            }
        });
    }
}
