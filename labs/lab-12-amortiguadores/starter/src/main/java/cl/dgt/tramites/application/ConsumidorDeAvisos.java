package cl.dgt.tramites.application;

import cl.dgt.tramites.application.evento.AvisoDeFolio;
import cl.dgt.tramites.config.AmqpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * Toma los avisos de la cola y los envía. Y lo hace <strong>de forma idempotente</strong>, porque va
 * a recibir duplicados — no como posibilidad remota, sino como hecho de la vida.
 *
 * <p><strong>«Exactly once» no existe.</strong> Ningún sistema de mensajería lo garantiza, y los que
 * dicen garantizarlo están describiendo otra cosa. Lo que existe es <em>at least once</em>: el
 * broker promete que el mensaje llega al menos una vez, y por tanto —a veces— dos.
 *
 * <p>Pasa por razones normalísimas, ninguna de ellas un fallo: este consumidor procesa el aviso, se
 * cae antes de confirmar (<em>ack</em>), y el broker —que no tiene forma de saber si alcanzó a
 * trabajar— se lo entrega a otro. El broker hizo lo correcto. El negocio recibió dos correos.
 *
 * <p>La respuesta no es pedirle al broker una garantía que no puede dar: es hacer que <strong>recibir
 * dos veces dé lo mismo que recibir una</strong>. Eso es idempotencia, y no es teoría nueva — ya la
 * implementaste en el Lab 06 (RN-05: reintentar la emisión devuelve el MISMO folio en vez de crear
 * otro). Mismo principio, otro transporte.
 *
 * <p><strong>El mensaje envenenado.</strong> Si el proceso lanza, Spring reintenta un número acotado
 * de veces (ver {@code application.yml}) y después lo rechaza definitivamente: el broker lo manda a
 * la DLQ por el {@code x-dead-letter-exchange} declarado en {@link AmqpConfig}. Eso es lo que impide
 * que un solo mensaje malo atasque la cola y deje sin avisos a todos los buenos que van detrás.
 */
@Service
public class ConsumidorDeAvisos {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorDeAvisos.class);

    /**
     * Marca de laboratorio para provocar el mensaje envenenado.
     *
     * <p>En un sistema real el veneno llega solo: un JSON corrupto, un contribuyente que ya no
     * existe, un campo que cambió de tipo entre dos versiones del productor. Aquí hace falta poder
     * provocarlo a voluntad para que el TODO_3 sea comprobable, así que se declara explícito y a la
     * vista en vez de esconderlo en una condición rebuscada.
     */
    public static final String RUT_ENVENENADO = "00000000-0";

    private final JdbcClient jdbc;
    private final NotificadorService notificador;

    public ConsumidorDeAvisos(JdbcClient jdbc, NotificadorService notificador) {
        this.jdbc = jdbc;
        this.notificador = notificador;
    }

    @RabbitListener(queues = AmqpConfig.COLA)
    public void recibir(AvisoDeFolio aviso) {
        if (RUT_ENVENENADO.equals(aviso.rut())) {
            // Falla SIEMPRE, haga lo que haga el reintento. Es la definición de mensaje envenenado:
            // no es un fallo transitorio que se arregle esperando, y por eso reintentar para siempre
            // sería un bucle infinito que además bloquea la cola.
            throw new IllegalStateException(
                    "Aviso " + aviso.avisoId() + ": el contribuyente " + aviso.rut() + " no existe");
        }

        // TODO_2 — Este consumidor procesa TODO lo que le llega, incluidos los duplicados.
        //
        //   Y le van a llegar. No como posibilidad remota: como hecho de la vida. "Exactly once" no
        //   existe — ningún broker lo garantiza, y los que dicen garantizarlo describen otra cosa.
        //   Lo que existe es "at least once": el mensaje llega AL MENOS una vez, y por tanto a
        //   veces dos.
        //
        //   Pasa por razones normalísimas, ninguna es un fallo: procesas el aviso, te caes antes de
        //   confirmar (ack), y el broker —que no puede saber si alcanzaste a trabajar— se lo entrega
        //   a otro. El broker hizo lo correcto. El contribuyente recibió dos correos.
        //
        //   La respuesta no es pedirle al broker una garantía que no puede dar: es hacer que recibir
        //   dos veces dé lo mismo que recibir una. Ya sabes hacerlo — es RN-05 del Lab 06, con otro
        //   transporte.
        //
        //   La tabla `aviso_procesado` (migración V6) está esperando, y el método de abajo también.
        //   Mira su implementación: mirar y marcar son UNA sola sentencia, igual que el candado del
        //   Lab 11. Escrito como "¿existe? entonces inserta", entre las dos líneas caben dos
        //   entregas concurrentes. {{TODO_2}}
        //
        // Pista 2: marcarComoProcesado(...) devuelve true solo la primera vez. Si devuelve false,
        //          este mensaje es un duplicado y no hay nada que hacer.
        notificador.notificar(aviso.rut(),
                "Su folio N° " + aviso.numero() + " fue emitido para el trámite " + aviso.tramiteId());
    }

    /**
     * Intenta reservar la clave de idempotencia. Devuelve {@code true} solo si es la primera vez.
     *
     * <p>Es la misma figura que el candado del Lab 11: mirar y marcar son <strong>una sola</strong>
     * sentencia atómica. Escrito como «¿existe? entonces inserta», entre las dos líneas caben dos
     * entregas concurrentes del mismo mensaje —cosa perfectamente posible con varios consumidores—
     * y las dos concluirían que les toca trabajar.
     */
    private boolean marcarComoProcesado(String clave) {
        return jdbc.sql("""
                INSERT INTO aviso_procesado (clave, procesado_en)
                VALUES (:clave, now())
                ON CONFLICT (clave) DO NOTHING
                """)
                .param("clave", clave)
                .update() == 1;
    }
}
