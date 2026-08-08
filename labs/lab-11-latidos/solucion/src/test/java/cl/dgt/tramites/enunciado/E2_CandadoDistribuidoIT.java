package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.CandadoDistribuido;
import cl.dgt.tramites.application.CierreService;
import cl.dgt.tramites.config.scheduling.CierreNocturnoJob;
import cl.dgt.tramites.infrastructure.repository.CierreDiarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · <strong>El candado distribuido.</strong>
 *
 * <p>El invariante del lab entero: <em>el trabajo se hace exactamente una vez</em>, aunque haya
 * N instancias despertando a la misma hora.
 *
 * <p><strong>Determinista, sin dormir.</strong> Todos los hilos se bloquean en una barrera
 * ({@link CountDownLatch}) y salen a la vez; el resultado que se afirma —«exactamente un ganador»—
 * es cierto para <em>cualquier</em> orden de ejecución, no para uno afortunado. Un test de
 * concurrencia que depende del tiempo no prueba nada: pasa en tu máquina y falla en el CI un
 * martes. Y {@code Thread.sleep} está prohibido por AU-05, que es la misma idea con dientes.
 */
@DisplayName("TODO_2 · N instancias compiten por el candado y el trabajo se hace UNA sola vez")
class E2_CandadoDistribuidoIT extends BaseLatidosIT {

    private static final int COMPETIDORES = 8;

    @Autowired
    CandadoDistribuido candado;

    @Autowired
    CierreNocturnoJob job;

    @Autowired
    CierreService cierre;

    @Autowired
    CierreDiarioRepository cierres;

    @Autowired
    org.springframework.jdbc.core.simple.JdbcClient jdbc;

    @BeforeEach
    void dejarElCandadoLibre() {
        // Reset DURO de la tabla, no `candado.liberar(...)`.
        //
        // `liberar` solo suelta lo que es suyo —y hace bien: una instancia no tiene derecho a
        // soltarle el candado a otra—. Pero eso significa que un candado dejado por otra prueba,
        // con TTL de dos minutos, sobrevive al @BeforeEach y hunde a la siguiente. Es lo que pasó
        // aquí: el test de expiración lo dejaba tomado por `el-que-sigue-vivo` y los ocho
        // competidores perdían legítimamente.
        //
        // La corrección va en el TEST, no en la API: el estado compartido se limpia desde fuera,
        // sin relajar la regla de propiedad que protege producción.
        jdbc.sql("DELETE FROM candado_tarea").update();
        cierre.reiniciarContadores();
    }

    @Test
    @DisplayName("ocho hilos piden el candado a la vez: exactamente UNO lo consigue")
    void soloUnoGanaElCandado() throws InterruptedException {
        AtomicInteger ganadores = new AtomicInteger();
        CountDownLatch listos = new CountDownLatch(COMPETIDORES);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch terminados = new CountDownLatch(COMPETIDORES);

        try (ExecutorService hilos = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < COMPETIDORES; i++) {
                String quien = "competidor-" + i;
                hilos.submit(() -> {
                    listos.countDown();
                    try {
                        salida.await();      // barrera: nadie empieza hasta que estén todos
                        if (candado.intentarTomar(CierreNocturnoJob.CANDADO, quien, Duration.ofMinutes(2))) {
                            ganadores.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        terminados.countDown();
                    }
                });
            }

            assertThat(listos.await(30, TimeUnit.SECONDS)).as("los competidores no llegaron a la barrera").isTrue();
            salida.countDown();
            assertThat(terminados.await(30, TimeUnit.SECONDS)).as("los competidores no terminaron").isTrue();
        }

        // El corazón del TODO_2. Si esto da 8, el candado no existe (cada instancia se creyó la
        // única). Si da 0, se tomó y nadie lo soltó. Solo 1 es correcto.
        assertThat(ganadores.get())
                .as("exactamente una instancia debe ganar: `INSERT ... ON CONFLICT` es atómico, "
                    + "no hay ventana entre mirar y tomar")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("ocho latidos simultáneos producen UN cierre, y sin solaparse")
    void elCierreSeEjecutaUnaSolaVez() throws InterruptedException {
        LocalDate hoy = LocalDate.now();
        int cierresAntes = cierres.findByFecha(hoy).size();

        CountDownLatch listos = new CountDownLatch(COMPETIDORES);
        CountDownLatch salida = new CountDownLatch(1);
        CountDownLatch terminados = new CountDownLatch(COMPETIDORES);

        try (ExecutorService hilos = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < COMPETIDORES; i++) {
                hilos.submit(() -> {
                    listos.countDown();
                    try {
                        salida.await();
                        job.latido();        // ocho instancias despertando a la misma hora
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        terminados.countDown();
                    }
                });
            }
            assertThat(listos.await(30, TimeUnit.SECONDS)).isTrue();
            salida.countDown();
            assertThat(terminados.await(60, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(cierre.ejecuciones())
                .as("el trabajo debe hacerse EXACTAMENTE una vez, no una por instancia")
                .isEqualTo(1);

        assertThat(cierres.findByFecha(hoy).size() - cierresAntes)
                .as("y debe quedar UNA fila de cierre para hoy. Dos filas del mismo día son el "
                    + "crimen: totales duplicados y dos avisos al mismo contribuyente")
                .isEqualTo(1);

        assertThat(cierre.maximoSimultaneas())
                .as("nunca hubo dos ejecuciones a la vez")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("el candado expira: si el que lo tomó muere, otro puede retomarlo")
    void elCandadoExpiraParaQueUnaMuerteNoBloqueeElSistemaParaSiempre() {
        // Se toma con una vigencia YA vencida: es la forma determinista de simular "el dueño murió"
        // sin esperar a que pase el tiempo. Nada de dormir: se declara vencido de entrada.
        assertThat(candado.intentarTomar(CierreNocturnoJob.CANDADO, "el-que-murio", Duration.ofSeconds(-1)))
                .as("la primera toma siempre gana: el candado estaba libre")
                .isTrue();

        // Sin expiración, este candado quedaría tomado por un proceso que ya no existe y el cierre
        // no volvería a correr JAMÁS — un fallo silencioso que solo se descubre cuando Carolina
        // pregunta por qué no llegó el resumen del martes.
        assertThat(candado.intentarTomar(CierreNocturnoJob.CANDADO, "el-que-sigue-vivo", Duration.ofMinutes(2)))
                .as("un candado vencido se puede arrebatar: el sistema se recupera solo")
                .isTrue();

        // Y ahora que está vigente, nadie más entra.
        assertThat(candado.intentarTomar(CierreNocturnoJob.CANDADO, "el-tercero", Duration.ofMinutes(2)))
                .as("un candado vigente NO se arrebata")
                .isFalse();
    }
}
