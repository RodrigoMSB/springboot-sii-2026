package cl.dgt.consolidado.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * La métrica de negocio, YA DECLARADA. No tienes que crearla: sólo usarla.
 *
 * <p>En tu {@code ConsolidadoService}, pide esta clase por constructor y llama a
 * {@link #emitidos()} cada vez que emitas un consolidado. Es una línea:
 *
 * <pre>
 *     contador.emitidos().increment();
 * </pre>
 *
 * <p>Se comprueba con {@code GET /actuator/metrics/dgt.consolidados.emitidos}.
 */
@Service
public class ContadorDeConsolidados {

    private final Counter emitidos;

    public ContadorDeConsolidados(MeterRegistry registro) {
        this.emitidos = Counter.builder("dgt.consolidados.emitidos")
                .description("Consolidados emitidos desde que arrancó la aplicación")
                .register(registro);
    }

    public Counter emitidos() {
        return emitidos;
    }
}
