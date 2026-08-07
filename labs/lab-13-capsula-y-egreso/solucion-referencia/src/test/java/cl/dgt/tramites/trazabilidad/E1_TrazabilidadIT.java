package cl.dgt.tramites.trazabilidad;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · El hilo de Ariadna (MDC). Cada petición lleva un {@code traceId}: se respeta si el
 * cliente lo trae, se genera si no, y TODA línea de log de esa petición lo comparte. Dos peticiones
 * distintas, dos traceId distintos. Es lo que convierte 200 líneas entrelazadas en una operación
 * seguible.
 */
class E1_TrazabilidadIT extends BaseTrazabilidadIT {

    @Test
    @DisplayName("el traceId de la petición se propaga y aparece en el MDC de los logs")
    void elTraceIdSePropagaAlMdc() {
        String carolina = bearer(CAROLINA);
        List<ILoggingEvent> auditoria = capturarLogs("AUDITORIA", () ->
                cliente().get().uri("/api/v1/tramites")
                        .header("Authorization", carolina)
                        .header("X-Trace-Id", "traza-de-carolina")
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().valueEquals("X-Trace-Id", "traza-de-carolina"));

        assertThat(auditoria).as("el aspecto auditó la operación").isNotEmpty();
        assertThat(auditoria)
                .as("toda línea de la petición lleva SU traceId en el MDC")
                .anyMatch(e -> "traza-de-carolina".equals(e.getMDCPropertyMap().get("traceId")));
    }

    @Test
    @DisplayName("dos peticiones sin traceId reciben dos generados, distintos")
    void dosPeticionesTraceIdsDistintos() {
        String carolina = bearer(CAROLINA);
        String t1 = trazaDeUnListado(carolina);
        String t2 = trazaDeUnListado(carolina);
        assertThat(t1).isNotBlank();
        assertThat(t2).isNotBlank();
        assertThat(t1).isNotEqualTo(t2);
    }

    private String trazaDeUnListado(String bearer) {
        return cliente().get().uri("/api/v1/tramites")
                .header("Authorization", bearer)
                .exchange().expectStatus().isOk()
                .returnResult(byte[].class)
                .getResponseHeaders().getFirst("X-Trace-Id");
    }
}
