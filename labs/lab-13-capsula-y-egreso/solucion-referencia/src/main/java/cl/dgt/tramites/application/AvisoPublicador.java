package cl.dgt.tramites.application;

import cl.dgt.tramites.application.evento.AvisoDeFolio;
import cl.dgt.tramites.config.AmqpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Entrega el aviso a la cola, y ahí se acaba su responsabilidad.
 *
 * <p><strong>La diferencia con el Lab 11, en una frase:</strong> una llamada directa exige que el
 * otro esté vivo <em>en el mismo instante</em>; una cola solo exige que exista. Antes, si el
 * servicio de avisos estaba caído, el mensaje se evaporaba en silencio — la API respondía 201, nadie
 * veía un error, y doscientos contribuyentes se quedaban sin saber que tenían folio. Y lo peor:
 * tampoco había forma de saber <em>cuáles</em> doscientos.
 *
 * <p>Ahora el aviso se deja en un sitio <strong>durable</strong>, fuera del proceso. Si el consumidor
 * no está, el mensaje espera. Si la DGT se reinicia, el mensaje sigue ahí. Si nadie lo procesa en
 * dos horas, se procesa a la tercera. La cola es el <em>amortiguador</em> entre dos sistemas con
 * ritmos distintos, y publicar en ella es una promesa mucho más barata de cumplir que «el otro
 * responde ahora».
 *
 * <p><strong>Lo que esto NO resuelve</strong>, y conviene decirlo antes de que alguien lo suponga:
 * si el broker estuviera caído en el momento de publicar, el aviso se perdería igual. Se ha movido
 * el punto de fallo a un sitio mucho más fiable —un broker con disco y réplicas— pero no se ha
 * eliminado. La garantía completa («ni un aviso perdido aunque el broker no esté») necesita
 * escribir el mensaje en la misma transacción que el folio y publicarlo después; se llama
 * <em>outbox</em>, se nombra en la teoría y no se teclea hoy.
 */
@Service
public class AvisoPublicador {

    private static final Logger log = LoggerFactory.getLogger(AvisoPublicador.class);

    private final RabbitTemplate rabbit;

    public AvisoPublicador(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    /**
     * Publica el aviso en el exchange. No espera a que nadie lo consuma.
     *
     * <p>Fíjate en que se publica al EXCHANGE, no a la cola: el productor no sabe —ni quiere saber—
     * quién escucha. Mañana se agrega un consumidor que actualiza un tablero y este archivo no se
     * toca. Es el desacople del evento del Lab 11, ahora cruzando el límite del proceso.
     */
    public void publicar(AvisoDeFolio aviso) {
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY, aviso);
        log.info("Aviso {} entregado a la cola (folio {})", aviso.avisoId(), aviso.numero());
    }
}
