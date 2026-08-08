package cl.dgt.tramites.mensajeria;

import cl.dgt.tramites.application.ConsumidorDeAvisos;
import cl.dgt.tramites.application.NotificadorService;
import cl.dgt.tramites.application.evento.AvisoDeFolio;
import cl.dgt.tramites.config.AmqpConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · <strong>La cola de los muertos.</strong>
 *
 * <p>Dos afirmaciones, y la segunda es la que de verdad importa:
 *
 * <ol>
 *   <li>El mensaje que <strong>siempre</strong> falla termina en la DLQ, <strong>con su causa</strong>.</li>
 *   <li>La cola principal <strong>sigue fluyendo</strong>: los mensajes buenos que van detrás del
 *       muerto se procesan igual.</li>
 * </ol>
 *
 * <p>Sin DLQ, un mensaje envenenado tiene dos destinos posibles y los dos son malos: se descarta en
 * silencio (perdiste el aviso y no lo sabes) o vuelve a la cola para siempre — y entonces
 * <strong>un solo mensaje malo deja sin avisos a todos los buenos</strong>. Esa segunda es la peor
 * de las dos, y es la que ocurre por omisión si nadie configura nada.
 */
@DisplayName("TODO_3 · el mensaje envenenado cae a la DLQ con su causa, y la cola sigue fluyendo")
class E3_LaColaDeLosMuertosIT extends BaseAmortiguadoresIT {

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
    @DisplayName("un aviso que siempre falla termina en la DLQ, y su causa viaja con él")
    void elEnvenenadoTerminaEnLaDlqConSuCausa() {
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-66601", 1L, 66601L, ConsumidorDeAvisos.RUT_ENVENENADO));

        // Reintenta el número acotado de veces que declara application.yml y, agotadas, se rinde:
        // el broker lo enruta por el x-dead-letter-exchange. Rendirse es la decisión correcta —un
        // mensaje envenenado no se cura esperando—.
        Awaitility.await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(mensajesEn(AmqpConfig.COLA_DLQ))
                        .as("tras agotar los reintentos, el mensaje debe caer a la DLQ")
                        .isEqualTo(1));

        assertThat(mensajesEn(AmqpConfig.COLA))
                .as("y NO puede quedarse dando vueltas en la cola principal")
                .isZero();

        // La causa. Una DLQ sin el porqué es un basurero; con él, es una bandeja de trabajo: alguien
        // la abre, lee qué pasó y decide si reprocesa, corrige o descarta.
        Message muerto = rabbit.receive(AmqpConfig.COLA_DLQ, 5_000);
        assertThat(muerto).as("el mensaje muerto debe poder leerse desde la DLQ").isNotNull();

        String cuerpo = new String(muerto.getBody(), StandardCharsets.UTF_8);
        assertThat(cuerpo)
                .as("el mensaje llega entero: se puede ver de qué aviso se trataba")
                .contains("folio-66601");

        assertThat(muerto.getMessageProperties().getHeaders())
                .as("el broker adjunta las cabeceras x-death con el recuento y el motivo del rechazo")
                .containsKey("x-death");
    }

    @Test
    @DisplayName("LO QUE IMPORTA: detrás del muerto, los avisos buenos siguen llegando")
    void elMuertoNoAtascaLaCola() {
        // El envenenado va PRIMERO, a propósito. Si el mensaje malo bloqueara la cola —que es lo que
        // pasa cuando se reencola sin tope—, los dos buenos de detrás no se procesarían nunca.
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-66602", 1L, 66602L, ConsumidorDeAvisos.RUT_ENVENENADO));
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-66603", 1L, 66603L, "11111111-1"));
        rabbit.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTING_KEY,
                new AvisoDeFolio("folio-66604", 1L, 66604L, "12345678-5"));

        Awaitility.await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("los avisos buenos deben entregarse aunque uno anterior estuviera "
                            + "envenenado: un mensaje malo NO puede dejar sin avisos a los demás")
                        .hasSize(2));

        assertThat(mensajesEn(AmqpConfig.COLA_DLQ))
                .as("y el envenenado, apartado en la DLQ")
                .isEqualTo(1);
    }
}
