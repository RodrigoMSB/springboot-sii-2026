package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.NotificadorService;
import cl.dgt.tramites.application.evento.AvisoDeFolio;
import cl.dgt.tramites.config.AmqpConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · <strong>El consumidor idempotente.</strong>
 *
 * <p>La afirmación: <strong>el mismo mensaje entregado dos veces produce un solo aviso.</strong>
 *
 * <p>No es una precaución exagerada. «Exactly once» no existe: ningún broker lo garantiza, y los que
 * dicen garantizarlo describen otra cosa. Lo que existe es <em>at least once</em>, y el duplicado
 * llega por razones normalísimas — el consumidor procesa el aviso, se cae antes de confirmar, y el
 * broker, que no tiene forma de saber si alcanzó a trabajar, se lo entrega a otro. El broker hizo lo
 * correcto; el contribuyente recibió dos correos.
 *
 * <p>Y esto ya lo sabes hacer: es RN-05 del Lab 06 —reintentar la emisión devuelve el MISMO folio en
 * vez de crear otro— con otro transporte. La idempotencia no es un truco de mensajería: es cómo se
 * sobrevive a un mundo donde los reintentos existen.
 */
@DisplayName("TODO_2 · el mismo aviso entregado dos veces produce UN solo envío")
class E2_ConsumidorIdempotenteIT extends BaseAmortiguadoresIT {

    @Autowired
    NotificadorService notificador;

    @Autowired
    RabbitTemplate rabbit;

    @BeforeEach
    void buzonLimpio() {
        vaciarColas();
        notificador.limpiar();
        encenderConsumidor();
    }

    @Test
    @DisplayName("dos entregas del MISMO mensaje: un solo aviso enviado")
    void elDuplicadoNoDuplicaElEfecto() {
        // El mismo aviso, con la misma clave de idempotencia, publicado dos veces. Es exactamente lo
        // que hace el broker cuando reentrega: el mensaje es idéntico, incluida su clave.
        AvisoDeFolio aviso = new AvisoDeFolio("folio-99001", 1L, 99001L, "11111111-1");

        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY, aviso);
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY, aviso);

        // Se espera a que el aviso llegue…
        Awaitility.await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(notificador.enviadas()).isNotEmpty());

        // …y luego se SOSTIENE la afirmación: sigue habiendo uno solo. Sin el `during`, esto podría
        // pasar por casualidad (mirando antes de que llegue el segundo). La afirmación negativa
        // —"no llega un segundo"— necesita una ventana, no un instante.
        Awaitility.await()
                .during(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(8))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("dos entregas del mismo hecho = un solo aviso. Si son dos, el "
                            + "contribuyente recibió el mismo correo dos veces por un reintento "
                            + "del broker que nadie provocó")
                        .hasSize(1));
    }

    @Test
    @DisplayName("dos avisos DISTINTOS sí producen dos envíos: la deduplicación no se pasa de lista")
    void laDeduplicacionNoSilenciaLoQueSiEsNuevo() {
        // El contraste necesario. Un consumidor que descarta todo también «no duplica», y sería
        // inútil. La clave identifica el HECHO, y dos folios distintos son dos hechos distintos.
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-99002", 1L, 99002L, "11111111-1"));
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-99003", 1L, 99003L, "11111111-1"));

        Awaitility.await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("dos hechos distintos = dos avisos")
                        .hasSize(2));
    }
}
