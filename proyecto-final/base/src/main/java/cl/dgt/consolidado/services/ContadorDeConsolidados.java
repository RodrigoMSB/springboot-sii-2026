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
 *
 * <p><b>Y ojo con qué prueba esa comprobación.</b> El contador se registra al arrancar, así que
 * ese endpoint responde <b>200</b> con un cuerpo de aspecto perfectamente sano —una medición
 * {@code COUNT} con {@code value: 0.0}— aunque nunca hayas llamado a {@code increment()}.
 * Que conteste no demuestra nada: lo que hay que mirar es que el número
 * <b>suba</b> después de pedir un consolidado. Si se queda en 0 pedido tras pedido, la línea
 * falta — y nada más te lo va a decir.
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
