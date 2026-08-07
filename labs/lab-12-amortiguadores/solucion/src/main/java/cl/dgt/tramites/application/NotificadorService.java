package cl.dgt.tramites.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Avisa al contribuyente. Es el efecto secundario clásico: importante, lento, y algo que el que
 * pidió la operación no tiene por qué esperar.
 *
 * <p><strong>Por qué asíncrono.</strong> Emitir un folio son milisegundos; avisar por correo son
 * cientos, y a veces segundos. Hacerlo dentro de la petición significa que el contribuyente mira
 * una rueda girando mientras un servidor de correo lo piensa. Peor: si el correo falla, el folio
 * —que ya se emitió y es válido— parecería haber fallado.
 *
 * <p><strong>Por qué hilos virtuales (Java 25).</strong> Esta carga es el caso de libro: pasa el
 * 99 % del tiempo <em>esperando</em> a que otro conteste, no calculando. Un hilo de plataforma
 * bloqueado en una espera es un megabyte de pila y un slot del sistema operativo gastados en no
 * hacer nada. Un hilo virtual que se bloquea se <em>desmonta</em> del hilo portador y devuelve la
 * máquina a quien la necesite. Por eso el pool deja de ser el recurso escaso.
 *
 * <p>El corolario práctico, que es lo que cambia el día a día: con hilos de plataforma se
 * dimensionaba el pool para no ahogar la máquina; con virtuales, ese dimensionamiento deja de ser
 * la palanca. Lo que <strong>no</strong> cambia es que el servicio del otro lado sigue teniendo un
 * límite: diez mil hilos virtuales golpeando un servidor de correo que aguanta cincuenta no es
 * paralelismo, es una denegación de servicio con tu firma. Los hilos virtuales quitan el cuello de
 * botella de TU lado, no del ajeno.
 *
 * <p><strong>Lo que NO son.</strong> No aceleran el cálculo: si la tarea quema CPU, un hilo virtual
 * no le da un núcleo más. Solo brillan cuando el trabajo es esperar.
 */
@Service
public class NotificadorService {

    private static final Logger log = LoggerFactory.getLogger(NotificadorService.class);

    /**
     * Las notificaciones enviadas, en memoria.
     *
     * <p>Es un doble de laboratorio, no un diseño: aquí iría el cliente del servicio de correo. Y
     * fíjate en lo que implica, porque es la semilla de la próxima sesión — <strong>si el proceso
     * se reinicia, esta cola desaparece y nadie sabe qué avisos se perdieron</strong>.
     */
    private final Queue<Aviso> enviadas = new ConcurrentLinkedQueue<>();

    /**
     * Un aviso enviado, con la huella del hilo que lo envió.
     *
     * <p>El hilo no es decorado: es lo que permite AFIRMAR que la asincronía ocurrió, en vez de
     * suponerlo. {@code hiloVirtual=false} en una notificación significa que {@code @Async} no
     * actuó — casi siempre porque la llamada no pasó por el proxy.
     */
    public record Aviso(String rut, String mensaje, String hilo, boolean hiloVirtual) {}

    /**
     * Envía el aviso, fuera del hilo de quien lo pidió.
     *
     * <p><strong>Devuelve {@code void}, y eso tiene una consecuencia que hay que saber:</strong> si
     * este método lanza una excepción, <em>nadie se entera</em>. No se propaga al llamador —el
     * llamador ya siguió su camino hace rato— y sin un manejador configurado se pierde. Por eso el
     * {@code try/catch} de aquí abajo no es paranoia: en un método {@code @Async void}, el
     * {@code catch} es el único lugar donde ese error todavía existe.
     *
     * <p>Si necesitaras el resultado o el error en el llamador, la firma tendría que devolver
     * {@code CompletableFuture<T>}: ahí la excepción viaja dentro del futuro y quien haga
     * {@code join()} la recibe.
     */
    @Async("ejecutorVirtual")
    public void notificar(String rut, String mensaje) {
        try {
            Thread hilo = Thread.currentThread();
            enviadas.add(new Aviso(rut, mensaje, hilo.toString(), hilo.isVirtual()));
            log.info("Notificación enviada a {} en el hilo {} (virtual={})",
                    enmascarar(rut), hilo, hilo.isVirtual());
        } catch (RuntimeException fallo) {
            // Un @Async void que revienta se traga el error a menos que alguien lo recoja aquí.
            // Registrar y NO re-lanzar: re-lanzar en un hilo asíncrono no le llega a nadie.
            log.error("No pude notificar a {}: {}", enmascarar(rut), fallo.toString());
        }
    }

    /**
     * Demuestra <strong>la trampa del proxy</strong>, por tercera vez en el curso.
     *
     * <p>Este método llama a {@link #notificar(String, String)} con {@code this}. Esa llamada NO
     * pasa por el proxy de Spring, así que {@code @Async} no se aplica: se ejecuta en el mismo
     * hilo, de forma síncrona, y nadie avisa de nada. Es exactamente el mismo límite que tiene
     * {@code @Transactional} (Lab 06) y que tenía el aspecto de auditoría (Lab 09).
     *
     * <p>A estas alturas del curso ya no es una curiosidad: es cómo funciona Spring. Las
     * anotaciones no son magia del compilador — son un objeto envolviendo a otro, y desde dentro
     * del objeto envuelto el envoltorio no existe.
     */
    public void notificarPorDentro(String rut, String mensaje) {
        this.notificar(rut, mensaje);   // llamada interna: el proxy no interviene
    }

    /** Lo enviado hasta ahora. Lo consultan los tests y el guion del crimen. */
    public List<Aviso> enviadas() {
        return List.copyOf(enviadas);
    }

    /** Cuántos avisos llevan el texto dado. Lo usa el crimen para contar el aviso duplicado. */
    public long cuantosCon(String fragmento) {
        return enviadas.stream().filter(a -> a.mensaje().contains(fragmento)).count();
    }

    public void limpiar() {
        enviadas.clear();
    }

    /** Misma doctrina del Lab 09: en un log, el RUT va parcial. */
    private String enmascarar(String rut) {
        return rut == null || rut.length() < 4 ? "***" : rut.substring(0, 3) + "***";
    }
}
