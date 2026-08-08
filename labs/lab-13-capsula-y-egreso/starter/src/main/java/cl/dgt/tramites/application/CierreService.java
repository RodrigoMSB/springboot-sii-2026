package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.CierreDiario;
import cl.dgt.tramites.infrastructure.repository.CierreDiarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * El cierre nocturno: consolida los trámites del día y avisa.
 *
 * <p>Aquí no hay candado ni reloj. Este servicio sabe hacer el trabajo <strong>una vez</strong>, y
 * nada más; quién lo dispara y cuántos tienen derecho a dispararlo es problema de
 * {@link cl.dgt.tramites.config.scheduling.CierreNocturnoJob}. Separarlo no es ceremonia: hace que
 * el trabajo se pueda probar sin esperar a que dé la hora, y que el candado se pueda probar sin
 * hacer el trabajo.
 */
@Service
public class CierreService {

    private static final Logger log = LoggerFactory.getLogger(CierreService.class);

    private final JdbcClient jdbc;
    private final CierreDiarioRepository cierres;
    private final NotificadorService notificador;

    /**
     * Instrumentación de concurrencia. Cuenta cuántas ejecuciones hay <em>a la vez</em> y guarda el
     * máximo visto.
     *
     * <p>No es andamiaje de test: es la única forma de afirmar «las ejecuciones no se solapan» con
     * un número en vez de con una promesa. Si este máximo llega a 2, el candado no sirvió — y en
     * producción eso significa dos cierres, dos correos y unos totales que no cuadran con nada.
     */
    private final AtomicInteger enCurso = new AtomicInteger();
    private final AtomicInteger maximoSimultaneas = new AtomicInteger();
    private final AtomicInteger ejecuciones = new AtomicInteger();

    public CierreService(JdbcClient jdbc, CierreDiarioRepository cierres, NotificadorService notificador) {
        this.jdbc = jdbc;
        this.cierres = cierres;
        this.notificador = notificador;
    }

    /**
     * Consolida el día y deja la fila del cierre.
     *
     * <p>Es {@code @Transactional}: la fila del cierre y todo lo que se escriba con ella viajan
     * juntas. La notificación NO viaja dentro —sale por el notificador asíncrono—, y esa asimetría
     * es intencional: el dato se guarda o no se guarda; el aviso, en cambio, ya no se puede
     * «desenviar».
     */
    @Transactional
    public CierreDiario ejecutarCierre(LocalDate fecha, String instancia) {
        int simultaneas = enCurso.incrementAndGet();
        maximoSimultaneas.accumulateAndGet(simultaneas, Math::max);
        try {
            log.info("Cierre nocturno de {} — ejecutando en la instancia {}", fecha, instancia);

            // Una consulta agregada, sin cargar entidades: la misma doctrina del reporte del Lab 10.
            Resumen resumen = jdbc.sql("""
                    SELECT COUNT(DISTINCT t.id)                    AS tramites,
                           COALESCE(SUM(l.monto), 0)               AS total
                      FROM tramite t
                      LEFT JOIN formulario29 f ON f.tramite_id = t.id
                      LEFT JOIN linea_f29   l ON l.formulario29_id = f.id
                    """)
                    .query(Resumen.class)
                    .single();

            CierreDiario cierre = cierres.save(new CierreDiario(
                    fecha, resumen.tramites(), resumen.total(), LocalDateTime.now(), instancia));

            ejecuciones.incrementAndGet();
            log.info("Cierre {} consolidado: {} trámites, {} declarado (instancia {})",
                    cierre.getId(), resumen.tramites(), resumen.total(), instancia);

            // El aviso del resumen a la jefatura. Asíncrono: nadie espera al servidor de correo.
            notificador.notificar("9876543-2",
                    "Cierre del " + fecha + ": " + resumen.tramites() + " trámites, "
                    + resumen.total() + " declarado.");

            return cierre;
        } finally {
            enCurso.decrementAndGet();
        }
    }

    /** Cuántas ejecuciones hubo. */
    public int ejecuciones() {
        return ejecuciones.get();
    }

    /** El máximo de ejecuciones simultáneas observado. Debe ser 1, siempre. */
    public int maximoSimultaneas() {
        return maximoSimultaneas.get();
    }

    public void reiniciarContadores() {
        ejecuciones.set(0);
        maximoSimultaneas.set(0);
    }

    /** Un dato, no una entidad. */
    public record Resumen(int tramites, long total) {}
}
