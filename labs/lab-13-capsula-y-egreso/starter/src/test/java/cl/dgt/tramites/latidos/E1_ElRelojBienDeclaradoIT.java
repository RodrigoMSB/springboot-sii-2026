package cl.dgt.tramites.latidos;

import cl.dgt.tramites.config.scheduling.CierreNocturnoJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · <strong>El reloj bien declarado.</strong>
 *
 * <p>Se inspeccionan las tareas que Spring registró de verdad ({@link ScheduledTaskHolder}), no el
 * texto del código fuente. Es la diferencia entre comprobar lo que el planificador <em>va a hacer</em>
 * y comprobar lo que alguien <em>escribió</em>; solo lo primero es una prueba.
 *
 * <p><strong>Por qué el solapamiento se afirma por declaración y no cronometrando.</strong> Para
 * ver dos ejecuciones pisándose habría que alargar artificialmente el trabajo, y alargar sin
 * {@code Thread.sleep} —AU-05 lo prohíbe, y con razón: un test que duerme es una apuesta— exigiría
 * meter una traba en el código de producción solo para el test. La propiedad que de verdad importa
 * es binaria y está en la declaración: <strong>{@code fixedDelay} mide de fin a inicio y por
 * construcción no puede solaparse; {@code fixedRate} mide de inicio a inicio y sí puede</strong>.
 * Aquí se afirma esa propiedad. Y el invariante de ejecución —«el trabajo ocurre una sola vez»— lo
 * mide {@code E2} con concurrencia real.
 */
@DisplayName("TODO_1 · el cierre se declara con fixedDelay (no fixedRate) y el cron lleva zona horaria")
class E1_ElRelojBienDeclaradoIT extends BaseLatidosIT {

    @Autowired
    List<ScheduledTaskHolder> registros;

    private Set<ScheduledTask> tareas() {
        return registros.stream()
                .flatMap(r -> r.getScheduledTasks().stream())
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    @DisplayName("el latido del cierre es una tarea de FIXED DELAY: de fin a inicio, jamás se solapa")
    void elLatidoEsDeIntervaloEntreFinYComienzo() {
        List<Task> intervalos = tareas().stream()
                .map(ScheduledTask::getTask)
                .filter(t -> t instanceof FixedDelayTask || t instanceof FixedRateTask)
                .toList();

        assertThat(intervalos)
                .as("no hay ninguna tarea programada por intervalo: ¿falta @EnableScheduling o el @Scheduled?")
                .isNotEmpty();

        // La afirmación dura. fixedRate mide de INICIO a INICIO: si el cierre tarda más que el
        // intervalo, la siguiente sale igual y —con el pool de 4 hilos que declara application.yml—
        // se solapan. Dos cierres a la vez en la MISMA instancia, sin necesidad de un segundo
        // servidor. fixedDelay mide de FIN a INICIO: no puede pasar, dure lo que dure el trabajo.
        assertThat(intervalos)
                .as("el cierre escribe y su duración crece con los años de declaraciones: eso es "
                    + "fixedDelay. Con fixedRate, el día que el trabajo dure más que el intervalo, "
                    + "dos ejecuciones se pisan y los totales dejan de cuadrar")
                .noneMatch(FixedRateTask.class::isInstance)
                .anyMatch(FixedDelayTask.class::isInstance);
    }

    @Test
    @DisplayName("el cron de producción declara zona America/Santiago explícita")
    void elCronDeclaraLaZonaHoraria() throws NoSuchMethodException {
        // La tarea cron está registrada de verdad (apunta a las 3 AM, así que no salta en la suite).
        List<CronTask> crons = tareas().stream()
                .map(ScheduledTask::getTask)
                .filter(CronTask.class::isInstance)
                .map(CronTask.class::cast)
                .toList();

        assertThat(crons)
                .as("no hay ninguna tarea cron registrada: el cierre de producción debe declararse")
                .isNotEmpty();

        // Y la zona: se lee de la anotación, que es donde se declara. Sin `zone`, el cron usa la
        // del SERVIDOR — que bien puede estar en UTC mientras la DGT está en Santiago: el «cierre
        // de las 3 AM» correría a medianoche. Y como Chile mueve la hora en marzo y septiembre, el
        // fallo no aparece el día del despliegue: aparece un domingo, meses después.
        Method metodo = CierreNocturnoJob.class.getMethod("cierreDeLasTres");
        Scheduled anotacion = metodo.getAnnotation(Scheduled.class);

        assertThat(anotacion)
                .as("cierreDeLasTres() debe estar anotado con @Scheduled")
                .isNotNull();
        assertThat(anotacion.zone())
                .as("la zona debe ser explícita: omitirla es un error que solo se nota en marzo")
                .isEqualTo(CierreNocturnoJob.ZONA)
                .isEqualTo("America/Santiago");
    }
}
