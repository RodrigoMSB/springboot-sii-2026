package cl.dgt.tramites.trazabilidad;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · Log estructurado en JSON. Una línea de log, renderizada con el encoder de la consola,
 * debe PARSEAR como JSON y contener el {@code traceId}. Un agregador (ELK, Loki) indexa objetos,
 * no texto plano. Con texto plano, este test se pone rojo al no poder parsear.
 */
class E2_LogJsonIT extends BaseTrazabilidadIT {

    @Test
    @DisplayName("una línea de log parsea como JSON y contiene el traceId")
    void elLogEsJsonConTraceId() throws Exception {
        List<ILoggingEvent> eventos = capturarLogs("cl.dgt.tramites.prueba.json", () -> {
            MDC.put("traceId", "traza-json-123");
            LoggerFactory.getLogger("cl.dgt.tramites.prueba.json").info("linea de prueba");
            MDC.remove("traceId");
        });
        assertThat(eventos).isNotEmpty();

        String linea = renderizarConEncoderDeConsola(eventos.get(0));
        JsonNode json = new ObjectMapper().readTree(linea);   // parsea como JSON o revienta

        assertThat(json.isObject()).as("cada línea es un objeto JSON").isTrue();
        assertThat(linea).as("el traceId viaja en el JSON").contains("traza-json-123");
    }
}
