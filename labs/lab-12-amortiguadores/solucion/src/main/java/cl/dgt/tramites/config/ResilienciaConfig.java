package cl.dgt.tramites.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * El interruptor automático de la llamada a Tesorería.
 *
 * <p><strong>Qué problema resuelve, y por qué el timeout del Lab 08 no bastaba.</strong> Aquel
 * timeout impide que <em>una</em> llamada espere para siempre: es un presupuesto de espera por
 * petición. Pero si TESO lleva veinte minutos caído, cada petición sigue gastando su presupuesto
 * completo —800 ms de espera inútil— y, sobre todo, <strong>sigue golpeando a un servicio que ya
 * está en el suelo</strong>. Mil peticiones por minuto contra un sistema que intenta levantarse es
 * lo que convierte una caída de dos minutos en una de veinte.
 *
 * <p>El circuit breaker cuenta los fallos y, pasado el umbral, <strong>abre</strong>: deja de
 * intentar. Las llamadas siguientes fallan <em>inmediatamente</em>, sin tocar la red. Dos ganancias
 * a la vez: el que llama no espera, y el que está caído deja de recibir golpes y puede recuperarse.
 *
 * <h2>Los tres estados</h2>
 * <ul>
 *   <li><strong>CLOSED</strong> — todo pasa. Se cuentan los fallos.</li>
 *   <li><strong>OPEN</strong> — nada pasa. Se falla al instante, sin red. Es el estado que
 *       <em>protege al otro</em>.</li>
 *   <li><strong>HALF_OPEN</strong> — tras la espera, deja pasar unas pocas de prueba. Si van bien,
 *       vuelve a CLOSED; si no, vuelve a OPEN. Es lo que hace que el sistema se recupere
 *       <strong>solo</strong>, sin que nadie tenga que ir a reiniciar nada.</li>
 * </ul>
 *
 * <h2>Por qué el núcleo de Resilience4j y no su starter</h2>
 * <p>{@code resilience4j-spring-boot3} es el starter de <em>Boot 3</em>: su autoconfiguración está
 * escrita contra APIs que Boot 4 reorganizó — el mismo tipo de renombre que ya nos mordió con
 * {@code spring-boot-starter-aop}, {@code webmvc-test} y {@code testcontainers-postgresql}. El
 * núcleo, en cambio, no depende de Spring en absoluto. Y declarándolo a mano los estados quedan a la
 * vista y el test los puede afirmar, que es la misma doctrina del {@code HealthIndicator} del
 * Lab 10: escribirlo una vez para ver el contrato.
 *
 * <h2>Primitivas nativas de Framework 7 vs. Resilience4j</h2>
 * <p>Spring Framework 7 trae ya {@code @Retryable} y {@code @ConcurrencyLimit}
 * ({@code org.springframework.resilience.annotation}), y para muchos casos <strong>bastan</strong>:
 * si lo único que necesitas es reintentar una operación idempotente un par de veces, o limitar
 * cuántas llamadas concurrentes salen hacia un servicio frágil, no metas una librería. El criterio:
 *
 * <table border="1">
 *   <caption>Cuándo basta cada uno</caption>
 *   <tr><th>Necesito…</th><th>Con qué</th></tr>
 *   <tr><td>Reintentar lo transitorio</td><td>{@code @Retryable} nativo</td></tr>
 *   <tr><td>No saturar al de enfrente</td><td>{@code @ConcurrencyLimit} nativo</td></tr>
 *   <tr><td><strong>Dejar de intentar</strong> y recuperarme solo</td><td>Resilience4j (no hay
 *       circuit breaker nativo)</td></tr>
 *   <tr><td>Rate limiter, bulkhead, time limiter, métricas de todo eso</td><td>Resilience4j</td></tr>
 * </table>
 */
@Configuration
public class ResilienciaConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienciaConfig.class);

    /** Nombre del circuito. Lo nombran el adaptador y el test. */
    public static final String CIRCUITO_TESO = "tesoreria";

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Cuenta sobre las últimas N llamadas, no sobre un porcentaje de todo el histórico:
                // una ventana deslizante reacciona a lo que pasa AHORA.
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                // No abrir con una golondrina: hacen falta al menos 4 llamadas para tener opinión.
                // Sin esto, el primer fallo tras arrancar abriría el circuito con una muestra de 1.
                .minimumNumberOfCalls(4)
                // Umbral alto y deliberado: TESO es una dependencia de la que ya sabemos degradar
                // (Lab 08), así que se abre cuando está claramente caído, no ante un hipo.
                .failureRateThreshold(50.0f)
                // Cuánto se queda abierto antes de volver a probar. Corto en el laboratorio para que
                // la recuperación se pueda ver en una sesión; en producción, decenas de segundos.
                .waitDurationInOpenState(Duration.ofSeconds(2))
                // Cuántas de prueba deja pasar en HALF_OPEN.
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        CircuitBreakerRegistry registro = CircuitBreakerRegistry.of(config);

        // El cambio de estado se registra SIEMPRE. Un circuito que se abre en silencio es un
        // incidente invisible: el servicio "va bien" —responde rápido— mientras no hace nada.
        // Es la doctrina del Lab 10: lo que no se ve, no se opera.
        registro.circuitBreaker(CIRCUITO_TESO).getEventPublisher()
                .onStateTransition(e -> log.warn("Circuito {}: {} -> {}",
                        e.getCircuitBreakerName(),
                        e.getStateTransition().getFromState(),
                        e.getStateTransition().getToState()));

        return registro;
    }

    @Bean
    CircuitBreaker circuitoTesoreria(CircuitBreakerRegistry registro) {
        return registro.circuitBreaker(CIRCUITO_TESO);
    }
}
