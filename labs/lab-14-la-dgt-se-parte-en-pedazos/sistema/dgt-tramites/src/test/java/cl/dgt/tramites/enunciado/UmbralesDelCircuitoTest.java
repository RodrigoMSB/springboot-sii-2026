package cl.dgt.tramites.enunciado;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

/**
 * EL CRITERIO DE ACEPTACIÓN DEL LABORATORIO.
 *
 * <p>Este test lee el archivo de configuración de verdad —el que el alumno edita,
 * {@code sistema/config-repo/dgt-tramites.yml}— construye con él un circuit
 * breaker real de Resilience4j, y le da el trato que va a recibir en el
 * escenario del laboratorio: diez llamadas seguidas que fallan.
 *
 * <p>Y entonces pregunta una sola cosa: <strong>¿abrió?</strong>
 *
 * <p>Con los umbrales sin declarar, Resilience4j usa sus valores por defecto, que
 * exigen <strong>cien</strong> llamadas antes de emitir juicio. Diez no bastan,
 * el circuito se queda cerrado, y este test se pone rojo. Con umbrales pensados
 * para el tamaño real del escenario, abre, y se pone verde.
 *
 * <p><strong>Este criterio no se aprueba tecleando más código</strong> (§7.4.2 de
 * la SPEC-000): no hay ninguna clase que escribir. Se aprueba entendiendo qué
 * mide una ventana deslizante y eligiendo cuatro números que quepan en el
 * problema que tienes delante.
 *
 * <h2>Por qué aquí no hay ni un {@code Thread.sleep}</h2>
 *
 * <p>Porque un test que duerme es un test que a veces falla en la máquina de
 * otro. Todo lo de aquí es contable, no cronometrable:
 *
 * <ul>
 *   <li>el estado del circuito se lee, no se espera;</li>
 *   <li>«falla rápido» no se mide con un reloj, se demuestra con un
 *       <em>contador</em>: cuando el circuito abre, la operación protegida deja
 *       de invocarse. Cero invocaciones es una prueba más fuerte que
 *       «tardó poco», y no depende de lo cargado que esté el portátil.</li>
 * </ul>
 *
 * <p>El cronómetro sí existe, pero vive donde tiene sentido: en
 * {@code bin/start-lab.sh --contribuyentes-lento}, contra el sistema levantado,
 * donde el alumno ve los milisegundos caer con sus ojos.
 */
@DisplayName("Los umbrales del circuito sirven para el escenario del laboratorio")
class UmbralesDelCircuitoTest {

    /**
     * Cuántas llamadas fallidas tiene el escenario del laboratorio.
     *
     * <p>No es un número inventado: en el bloque 2 el alumno lanza del orden de
     * ocho a diez peticiones contra un proveedor caído o lento. Si el circuito
     * necesita más que eso para decidirse, en esta clase no abre nunca — y en el
     * turno de guardia de un servicio con poco tráfico, tampoco.
     */
    private static final int LLAMADAS_DEL_ESCENARIO = 10;

    /** Los umbrales leídos del archivo del alumno. */
    private static Umbrales umbrales;

    /** De dónde se leyó, para poder decirlo en el mensaje de error. */
    private static Path archivo;

    /**
     * Los cinco umbrales que gobiernan un circuit breaker.
     *
     * <p>Todos {@code null}-ables a propósito: {@code null} significa «el alumno
     * no lo declaró», y en ese caso se deja el valor por defecto de
     * Resilience4j. Así el test reproduce exactamente lo que hace el framework
     * en tiempo de ejecución, sin inventarse nada.
     */
    record Umbrales(Integer slidingWindowSize,
                    Integer minimumNumberOfCalls,
                    Float failureRateThreshold,
                    Duration waitDurationInOpenState,
                    Integer permittedNumberOfCallsInHalfOpenState) {
    }

    @BeforeAll
    static void leerLaConfiguracionDelAlumno() throws IOException {
        // Qué config-repo mirar. Por defecto el del alumno; `90-validar.sh --dir
        // solucion` apunta al de referencia. El mismo test juzga a los dos: no
        // hay dos verdades (P-14 del ADN).
        String repo = System.getProperty("dgt.config-repo", "../config-repo");
        archivo = Path.of(repo, "dgt-tramites.yml").toAbsolutePath().normalize();

        assertThat(Files.exists(archivo))
                .as("No encuentro %s. ¿Lo borraste? Recupéralo con ./bin/95-recuperar.sh", archivo)
                .isTrue();

        List<PropertySource<?>> fuentes =
                new YamlPropertySourceLoader().load("config-repo", new FileSystemResource(archivo.toFile()));
        MutablePropertySources todas = new MutablePropertySources();
        fuentes.forEach(todas::addLast);

        // Binder sin resolutor de marcadores: el archivo tiene ${DB_URL} y
        // compañía, y no queremos que intente resolverlos. Solo se enlaza la
        // rama del circuito, que son números y nada más.
        umbrales = new Binder(ConfigurationPropertySources.from(todas))
                .bind("resilience4j.circuitbreaker.instances.contribuyentes", Bindable.of(Umbrales.class))
                .orElseGet(() -> new Umbrales(null, null, null, null, null));
    }

    @Test
    @DisplayName("con el proveedor caído, el circuito ABRE dentro del escenario del lab")
    void elCircuitoAbreDentroDelEscenario() {
        AtomicInteger invocaciones = new AtomicInteger();
        CircuitBreaker circuito = construirCircuito();

        for (int i = 0; i < LLAMADAS_DEL_ESCENARIO; i++) {
            llamarIgnorandoElFallo(circuito, invocaciones);
        }

        assertThat(circuito.getState())
                .as("""
                    Tras %d llamadas fallidas seguidas, el circuito sigue en %s.

                    Eso es un circuit breaker decorativo: está declarado, sale en el
                    diagrama, y no va a protegerte nunca — porque con los valores por
                    defecto necesita CIEN llamadas antes de emitir juicio, y en este
                    escenario no hay cien.

                    Los cuatro umbrales van en:
                      %s

                    Ahora mismo el circuito está configurado así:
                      ventana .................. %s
                      mínimo de llamadas ....... %s
                      umbral de fallo .......... %s
                      tiempo abierto ........... %s
                      llamadas en medio abierto  %s
                    (los nulos son los que no declaraste; ahí manda el valor por defecto)
                    """,
                    LLAMADAS_DEL_ESCENARIO, circuito.getState(), archivo,
                    umbrales.slidingWindowSize(), umbrales.minimumNumberOfCalls(),
                    umbrales.failureRateThreshold(), umbrales.waitDurationInOpenState(),
                    umbrales.permittedNumberOfCallsInHalfOpenState())
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("una vez abierto, FALLA RÁPIDO: deja de llamar al servicio caído")
    void abiertoDejaDeLlamar() {
        AtomicInteger invocaciones = new AtomicInteger();
        CircuitBreaker circuito = construirCircuito();

        for (int i = 0; i < LLAMADAS_DEL_ESCENARIO; i++) {
            llamarIgnorandoElFallo(circuito, invocaciones);
        }
        int invocacionesAlAbrir = invocaciones.get();

        // Cinco llamadas más, con el circuito ya abierto.
        for (int i = 0; i < 5; i++) {
            llamarIgnorandoElFallo(circuito, invocaciones);
        }

        assertThat(invocaciones.get())
                .as("""
                    Con el circuito abierto, la operación protegida NO debe ejecutarse:
                    esa es toda la idea de «fallar rápido». Si el contador subió, es que
                    se sigue llamando al servicio caído — y entonces se sigue esperando
                    su timeout, se siguen gastando hilos y se le sigue echando tráfico
                    encima al que intenta levantarse.

                    Invocaciones al abrir: %d · después de 5 llamadas más: %d
                    """, invocacionesAlAbrir, invocaciones.get())
                .isEqualTo(invocacionesAlAbrir);
    }

    @Test
    @DisplayName("y lo dice: rechaza con CallNotPermittedException, no con el error del vecino")
    void abiertoRechazaConSuPropiaExcepcion() {
        AtomicInteger invocaciones = new AtomicInteger();
        CircuitBreaker circuito = construirCircuito();
        for (int i = 0; i < LLAMADAS_DEL_ESCENARIO; i++) {
            llamarIgnorandoElFallo(circuito, invocaciones);
        }

        Throwable capturada = null;
        try {
            circuito.executeSupplier(invocaciones::incrementAndGet);
        } catch (Throwable t) {
            capturada = t;
        }

        assertThat(capturada)
                .as("""
                    Un circuito abierto no reenvía el error del vecino: lanza el suyo.
                    Distinguirlos importa cuando estés leyendo el log a las tres de la
                    mañana — «connection refused» y «el circuito está abierto» son dos
                    problemas distintos con dos respuestas distintas.
                    """)
                .isInstanceOf(CallNotPermittedException.class);
    }

    // -------------------------------------------------------------------------
    //  Andamiaje
    // -------------------------------------------------------------------------

    /**
     * Construye un circuit breaker REAL con lo que el alumno declaró, dejando el
     * resto en los valores por defecto de Resilience4j.
     *
     * <p>Nada de reimplementar la lógica: se usa {@code CircuitBreakerConfig},
     * que es exactamente lo que usa la aplicación en producción. Un test que
     * reimplementa lo que prueba no prueba nada.
     */
    private CircuitBreaker construirCircuito() {
        CircuitBreakerConfig.Builder cfg = CircuitBreakerConfig.custom();
        if (umbrales.slidingWindowSize() != null) {
            cfg.slidingWindowSize(umbrales.slidingWindowSize());
        }
        if (umbrales.minimumNumberOfCalls() != null) {
            cfg.minimumNumberOfCalls(umbrales.minimumNumberOfCalls());
        }
        if (umbrales.failureRateThreshold() != null) {
            cfg.failureRateThreshold(umbrales.failureRateThreshold());
        }
        if (umbrales.waitDurationInOpenState() != null) {
            cfg.waitDurationInOpenState(umbrales.waitDurationInOpenState());
        }
        if (umbrales.permittedNumberOfCallsInHalfOpenState() != null) {
            cfg.permittedNumberOfCallsInHalfOpenState(umbrales.permittedNumberOfCallsInHalfOpenState());
        }
        return CircuitBreaker.of("contribuyentes", cfg.build());
    }

    /** Una llamada que siempre falla, como el proveedor caído del bloque 2. */
    private void llamarIgnorandoElFallo(CircuitBreaker circuito, AtomicInteger invocaciones) {
        try {
            circuito.executeSupplier(() -> {
                invocaciones.incrementAndGet();
                throw new IllegalStateException("el servicio de contribuyentes no contesta");
            });
        } catch (Exception esperado) {
            // Es el escenario: aquí todo falla. Lo que se mide es la REACCIÓN.
        }
    }
}
