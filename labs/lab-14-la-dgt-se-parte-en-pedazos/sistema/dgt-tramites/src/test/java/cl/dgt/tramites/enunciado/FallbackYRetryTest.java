package cl.dgt.tramites.enunciado;

import static org.assertj.core.api.Assertions.assertThat;

import cl.dgt.tramites.ConsultaDeContribuyentes;
import cl.dgt.tramites.ContribuyenteCliente;
import cl.dgt.tramites.FichaContribuyente;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Que el fallback DISPARE de verdad, y que el retry REINTENTE de verdad.
 *
 * <p>Los dos son fáciles de tener rotos sin enterarse, y las dos averías son
 * silenciosas:
 *
 * <ul>
 *   <li>Si la firma del método de fallback no cuadra exactamente —mismos
 *       parámetros más un {@code Throwable}—, Resilience4j no lo encuentra. La
 *       aplicación arranca perfectamente. Te enteras la primera vez que el
 *       vecino se cae, en producción.</li>
 *   <li>Si el {@code fallbackMethod} se pone en {@code @CircuitBreaker} en vez
 *       de en {@code @Retry}, el retry <strong>no reintenta nunca</strong>:
 *       el anillo interior convierte la excepción en una respuesta válida y el
 *       exterior concluye que salió bien a la primera. Sigue habiendo una
 *       anotación {@code @Retry} en el código, un número en la configuración y
 *       una línea en el diagrama. Y cero reintentos.</li>
 * </ul>
 *
 * <p>Este test levanta un contexto de Spring MÍNIMO —solo AOP y los aspectos de
 * Resilience4j— con un cliente de mentira que cuenta cuántas veces lo llaman. Ni
 * base de datos, ni Config Server, ni red: es determinista y tarda menos de un
 * segundo.
 */
@DisplayName("El fallback dispara y el retry reintenta")
class FallbackYRetryTest {

    private static final int MAX_INTENTOS = 2;

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AopAutoConfiguration.class,
                    RetryAutoConfiguration.class,
                    CircuitBreakerAutoConfiguration.class))
            .withUserConfiguration(ClienteQueSiempreFalla.class)
            .withPropertyValues(
                    "resilience4j.retry.instances.contribuyentes.max-attempts=" + MAX_INTENTOS,
                    // Sin espera entre reintentos: no hay nada que esperar en un
                    // test, y un test que duerme es un test que a veces falla.
                    "resilience4j.retry.instances.contribuyentes.wait-duration=1ms",
                    // Ventana grande para que el circuito NO abra aquí: lo que se
                    // mide en este test es el fallback y el retry, no el circuito.
                    // Un test que prueba dos cosas a la vez no dice cuál se rompió.
                    "resilience4j.circuitbreaker.instances.contribuyentes.minimum-number-of-calls=1000");

    @Test
    @DisplayName("cuando el otro servicio no contesta, devuelve la ficha degradada — y no lanza")
    void elFallbackDispara() {
        contexto.run(ctx -> {
            ConsultaDeContribuyentes consulta = ctx.getBean(ConsultaDeContribuyentes.class);

            FichaContribuyente ficha = consulta.buscar("11111111-1");

            assertThat(ficha)
                    .as("El fallback tiene que devolver algo, no propagar la excepción")
                    .isNotNull();
            assertThat(ficha.rut())
                    .as("El RUT es dato LOCAL: el fallback lo conoce y debe conservarlo")
                    .isEqualTo("11111111-1");
            assertThat(ficha.razonSocial())
                    .as("""
                        El nombre viene del otro servicio, que está caído: tiene que venir
                        nulo. Si aquí hubiera un texto inventado —«Desconocido», «N/D»— el
                        sistema estaría fabricando datos, que es peor que no tenerlos.
                        """)
                    .isNull();
        });
    }

    @Test
    @DisplayName("el retry reintenta de verdad: el fallback está en el anillo de fuera")
    void elRetryReintenta() {
        contexto.run(ctx -> {
            ConsultaDeContribuyentes consulta = ctx.getBean(ConsultaDeContribuyentes.class);
            AtomicInteger llamadas = ctx.getBean(AtomicInteger.class);
            llamadas.set(0);

            consulta.buscar("11111111-1");

            assertThat(llamadas.get())
                    .as("""
                        Se esperaban %d intentos contra el cliente y hubo %d.

                        Si hubo UNO solo, el fallbackMethod está en @CircuitBreaker en vez
                        de en @Retry. Resilience4j compone los aspectos como
                        Retry ( CircuitBreaker ( método ) ): con el fallback en el anillo
                        interior, el exterior nunca ve una excepción y nunca reintenta.
                        El @Retry queda de adorno.
                        """, MAX_INTENTOS, llamadas.get())
                    .isEqualTo(MAX_INTENTOS);
        });
    }

    /**
     * Un {@code dgt-contribuyentes} de mentira que nunca contesta y lleva la
     * cuenta de cuántas veces lo intentaron.
     */
    @Configuration(proxyBeanMethods = false)
    static class ClienteQueSiempreFalla {

        @Bean
        AtomicInteger contadorDeLlamadas() {
            return new AtomicInteger();
        }

        @Bean
        ContribuyenteCliente contribuyenteCliente(AtomicInteger contador) {
            return rut -> {
                contador.incrementAndGet();
                throw new IllegalStateException("connection refused: dgt-contribuyentes");
            };
        }

        @Bean
        ConsultaDeContribuyentes consultaDeContribuyentes(ContribuyenteCliente cliente) {
            return new ConsultaDeContribuyentes(cliente);
        }
    }
}
