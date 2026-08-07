package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.TesoreriaPort;
import cl.dgt.tramites.domain.exception.TesoreriaNoDisponibleException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TODO_4 · <strong>El circuit breaker.</strong>
 *
 * <p>Lo que se mide aquí es <strong>tiempo</strong>, y por una razón: la diferencia entre un
 * circuito cerrado y uno abierto no se ve en el resultado —las dos veces falla— sino en
 * <em>cuánto tarda en fallar</em>. Cerrado, cada llamada gasta su presupuesto de espera completo
 * (el timeout del Lab 08) y encima golpea a un servicio que está en el suelo. Abierto, la llamada
 * se rechaza al instante y sin tocar la red.
 *
 * <p>Dos ganancias a la vez, y conviene nombrarlas por separado: el que llama deja de esperar, y el
 * que está caído deja de recibir golpes. La segunda es la que acorta la caída — mil peticiones por
 * minuto contra un sistema que intenta levantarse es lo que convierte dos minutos de incidencia en
 * veinte.
 *
 * <p><strong>Este test no levanta WireMock</strong>: TESO no está, y esa ausencia ES el escenario.
 * Las llamadas fallan de verdad, contra una dirección que no responde.
 */
@DisplayName("TODO_4 · tras N fallos el circuito abre y las llamadas fallan RÁPIDO, sin tocar la red")
class E4_CircuitBreakerIT extends BaseAmortiguadoresIT {

    @Autowired
    TesoreriaPort tesoreria;

    @Autowired
    CircuitBreaker circuitoTesoreria;

    @BeforeEach
    void circuitoEnCero() {
        circuitoTesoreria.reset();
    }

    private long milisDeUnFalloDe(String referencia) {
        long inicio = System.nanoTime();
        assertThatThrownBy(() -> tesoreria.confirmarPago(referencia))
                .as("con TESO ausente, la llamada debe fallar (rápido o lento, pero fallar)")
                .isInstanceOf(TesoreriaNoDisponibleException.class);
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    @Test
    @DisplayName("el circuito arranca CERRADO y se abre tras acumular fallos")
    void tras4FallosElCircuitoAbre() {
        assertThat(circuitoTesoreria.getState())
                .as("al empezar, todo pasa: el circuito está cerrado")
                .isEqualTo(CircuitBreaker.State.CLOSED);

        // minimumNumberOfCalls = 4: hacen falta cuatro llamadas para tener opinión. Sin ese mínimo,
        // el primer fallo tras arrancar abriría el circuito con una muestra de uno.
        for (int i = 0; i < 4; i++) {
            milisDeUnFalloDe("ref-" + i);
        }

        assertThat(circuitoTesoreria.getState())
                .as("superado el umbral de fallos, el circuito debe ABRIRSE y dejar de intentar")
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("abierto, la llamada falla MUCHO más rápido: ya no toca la red")
    void abiertoFallaRapidoSinTocarLaRed() {
        long cerrado = milisDeUnFalloDe("medida-cerrado");
        for (int i = 0; i < 4; i++) {
            milisDeUnFalloDe("ref-" + i);
        }
        assertThat(circuitoTesoreria.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        long abierto = milisDeUnFalloDe("medida-abierto");

        // La afirmación en números. No se fija un umbral absoluto —eso sería frágil— sino la
        // RELACIÓN: rechazar sin salir a la red tiene que ser claramente más barato que intentarlo.
        assertThat(abierto)
                .as("con el circuito abierto la llamada se rechaza al instante: %d ms cerrado vs %d ms abierto",
                        cerrado, abierto)
                .isLessThan(50);

        assertThat(abierto)
                .as("y debe ser sensiblemente más rápido que cuando sí salía a la red (%d ms)", cerrado)
                .isLessThanOrEqualTo(cerrado);
    }

    @Test
    @DisplayName("pasada la ventana, el circuito prueba de nuevo (HALF_OPEN): se recupera solo")
    void trasLaVenatanaElCircuitoSeRecuperaSolo() {
        for (int i = 0; i < 4; i++) {
            milisDeUnFalloDe("ref-" + i);
        }
        assertThat(circuitoTesoreria.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // waitDurationInOpenState = 2 s, con transición automática. Se espera la CONDICIÓN, no un
        // número de milisegundos: Awaitility, nunca Thread.sleep (AU-05).
        //
        // Esto es lo que hace que el sistema se arregle SOLO. Sin HALF_OPEN, un circuito abierto se
        // quedaría abierto para siempre y alguien tendría que ir a reiniciar algo a mano — habrías
        // cambiado una caída por otra.
        Awaitility.await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(circuitoTesoreria.getState())
                        .as("tras la ventana, el circuito debe pasar a HALF_OPEN y volver a probar")
                        .isEqualTo(CircuitBreaker.State.HALF_OPEN));
    }
}
