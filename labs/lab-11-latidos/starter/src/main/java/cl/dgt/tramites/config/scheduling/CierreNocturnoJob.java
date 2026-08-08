package cl.dgt.tramites.config.scheduling;

import cl.dgt.tramites.application.CandadoDistribuido;
import cl.dgt.tramites.application.CierreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

/**
 * El reloj del cierre nocturno. Aquí vive el <em>cuándo</em> y el <em>quién tiene derecho</em>;
 * el trabajo en sí es de {@link CierreService}.
 *
 * <p><strong>{@code fixedDelay}, no {@code fixedRate}.</strong> Los dos suenan igual y no lo son:
 *
 * <ul>
 *   <li>{@code fixedRate} mide de <em>inicio a inicio</em>. Si el trabajo tarda más que el
 *       intervalo, la siguiente ejecución sale igual — y con un planificador de más de un hilo,
 *       <strong>se solapan</strong>. Dos cierres a la vez, en la misma instancia, sin necesidad
 *       de un segundo servidor.</li>
 *   <li>{@code fixedDelay} mide de <em>fin a inicio</em>. La siguiente empieza N milisegundos
 *       después de que la anterior TERMINÓ. Nunca se solapan, dure lo que dure el trabajo.</li>
 * </ul>
 *
 * <p>La regla práctica: si la tarea es idempotente y corta, {@code fixedRate} está bien. Si escribe
 * y su duración depende del volumen de datos —como este cierre, que crece con los años—,
 * {@code fixedDelay}. Elegir {@code fixedRate} «porque suena a que corre cada minuto» es el bug
 * que aparece el día que la base creció.
 *
 * <p><strong>El detalle del cron: la zona horaria.</strong> Un {@code cron} sin {@code zone} usa la
 * zona del sistema, que es la del servidor — y el servidor puede estar en UTC aunque la DGT esté en
 * Santiago. Con `0 0 3 * * *` y el servidor en UTC, el cierre «de las 3 AM» corre a medianoche.
 * Peor todavía: Chile cambia la hora en marzo y septiembre, así que un cierre que funcionó todo el
 * verano se corre una hora un domingo cualquiera y nadie relaciona una cosa con la otra. Declararla
 * explícita cuesta doce caracteres y ahorra ese día.
 */
@Component
public class CierreNocturnoJob {

    private static final Logger log = LoggerFactory.getLogger(CierreNocturnoJob.class);

    /** Nombre del candado. Uno por tarea programada. */
    public static final String CANDADO = "cierre-nocturno";

    /**
     * La zona del cron, explícita y en una constante para que el test del enunciado la nombre sin
     * copiar un literal. La DGT declara en horario de Chile continental, no en el del servidor.
     */
    public static final String ZONA = "America/Santiago";

    private final CierreService cierre;
    private final CandadoDistribuido candado;
    private final String instancia;
    private final Duration ttlCandado;

    public CierreNocturnoJob(CierreService cierre, CandadoDistribuido candado,
                             @Value("${dgt.instancia}") String instancia,
                             @Value("${dgt.cierre.ttl-candado:PT2M}") Duration ttlCandado) {
        this.cierre = cierre;
        this.candado = candado;
        this.instancia = instancia;
        this.ttlCandado = ttlCandado;
    }

    /**
     * El latido del cierre.
     *
     * <p>El intervalo real de producción llega por propiedad (`0 0 3 * * *` en el cron de abajo); en
     * el laboratorio se acorta a segundos para que el crimen se vea en clase sin esperar a la noche.
     *
     * <p><strong>El candado se toma ANTES de mirar siquiera si hay trabajo.</strong> La tentación es
     * al revés —comprobar si ya hay cierre del día y salir si lo hay— y es exactamente la ventana
     * de carrera del Lab 06: entre «miro» y «escribo», la otra instancia hace lo mismo y las dos
     * concluyen que les toca.
     */
    // TODO_1 — Este `fixedRate` está mal elegido.
    //
    //   fixedRate mide de INICIO a INICIO: si el cierre tarda más que el intervalo, la siguiente
    //   ejecución sale igual y —con el pool de 4 hilos que declara application.yml— se solapan.
    //   Dos cierres a la vez en la MISMA instancia, sin necesidad de un segundo servidor.
    //   fixedDelay mide de FIN a INICIO. Piensa cuál corresponde a un trabajo que ESCRIBE y cuya
    //   duración crece con los años de declaraciones.
    //
    // Pista 2: es un cambio de una palabra en la anotación.
    @Scheduled(
            fixedRateString = "${dgt.cierre.intervalo-ms:86400000}",
            initialDelayString = "${dgt.cierre.retardo-inicial-ms:5000}")
    public void latido() {
        // TODO_2 (segunda mitad) — Aquí falta preguntar si a ESTA instancia le toca.
        //
        //   Tal como está, el método hace el trabajo siempre. Con una instancia es correcto; con
        //   dos, el cierre corre dos veces, los totales se duplican y el mismo contribuyente recibe
        //   el aviso dos veces. Vívelo antes de arreglarlo:
        //
        //       ./bin/start-lab.sh --instancias 2
        //
        //   Toma el candado antes de trabajar, y suéltalo al terminar. Piensa DÓNDE va ese soltado
        //   para que una excepción a mitad del cierre no deje el sistema bloqueado.
        //
        // Pista 2: `candado` ya está inyectado, y CANDADO e `instancia` ya existen. La liberación
        //          quiere un finally.
        cierre.ejecutarCierre(LocalDate.now(), instancia);
    }

    /**
     * La variante de producción: todos los días a las 3 AM, <strong>hora de Santiago</strong>.
     *
     * <p>Convive con el latido de arriba sin estorbarle, y no por casualidad: las dos pasan por el
     * mismo candado, así que aunque coincidieran, el trabajo se haría una sola vez. Es la prueba
     * de que el candado protege <em>la tarea</em>, no <em>al planificador</em>.
     *
     * <p>El {@code zone} es el contenido de esta línea. Quítalo y el cron usa la zona del sistema —
     * que es la del servidor, y el servidor bien puede estar en UTC mientras la DGT está en
     * Santiago: el «cierre de las 3 AM» correría a medianoche. Y como Chile mueve la hora en marzo
     * y septiembre, el error no aparece el día del despliegue: aparece un domingo, tres meses
     * después, cuando nadie está mirando.
     */
    // TODO_1 (segunda mitad) — a este cron le falta la zona horaria.
    //
    //   Sin `zone`, usa la del SERVIDOR. Y el servidor puede estar en UTC mientras la DGT está en
    //   Santiago: el «cierre de las 3 AM» correría a medianoche. Peor: Chile mueve la hora en marzo
    //   y septiembre, así que un cierre que funcionó todo el verano se corre una hora un domingo
    //   cualquiera, y nadie relaciona una cosa con la otra.
    //
    // Pista 2: la constante ZONA de arriba ya tiene el valor. Falta el atributo.
    @Scheduled(cron = "${dgt.cierre.cron:0 0 3 * * *}")
    public void cierreDeLasTres() {
        latido();
    }
}
