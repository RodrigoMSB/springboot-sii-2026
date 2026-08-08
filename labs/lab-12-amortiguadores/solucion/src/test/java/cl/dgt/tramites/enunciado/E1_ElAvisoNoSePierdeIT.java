package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.NotificadorService;
import cl.dgt.tramites.config.AmqpConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · <strong>El aviso va a la cola.</strong>
 *
 * <p>El crimen del laboratorio, medido: con el servicio de avisos caído, el Lab 11 perdía el
 * mensaje <em>en silencio</em> —la API respondía 201, nadie veía un error, y no quedaba forma de
 * saber cuáles avisos se habían evaporado—.
 *
 * <p>Con una cola de por medio, «el destinatario está caído» deja de ser un problema del emisor. Una
 * llamada directa exige que el otro esté vivo <strong>en el mismo instante</strong>; una cola solo
 * exige que exista.
 */
@DisplayName("TODO_1 · con el consumidor caído, el aviso ESPERA en la cola en vez de evaporarse")
class E1_ElAvisoNoSePierdeIT extends BaseAmortiguadoresIT {

    @Autowired
    NotificadorService notificador;

    @BeforeEach
    void buzonLimpio() {
        vaciarColas();
        notificador.limpiar();
    }

    @AfterEach
    void dejarloComoEstaba() {
        encenderConsumidor();
    }

    @Test
    @DisplayName("el servicio de avisos está caído: la API responde 201 y el mensaje queda en la cola")
    void conElConsumidorCaidoElMensajeEsperaEnLaCola() {
        apagarConsumidor();   // "el servicio de avisos está caído"

        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        long antes = System.nanoTime();
        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isCreated();
        long tardanzaMs = (System.nanoTime() - antes) / 1_000_000;

        // 1. La API no se entera ni le importa. Igual de rápida que siempre.
        assertThat(tardanzaMs)
                .as("publicar en una cola no espera a nadie: la API responde igual de rápido")
                .isLessThan(5_000);

        // 2. Y el aviso NO se perdió: está ahí, esperando. Esta línea es toda la diferencia con el
        //    Lab 11, donde en este punto el mensaje ya no existía en ninguna parte.
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(mensajesEn(AmqpConfig.COLA))
                        .as("el aviso debe estar ESPERANDO en la cola, no evaporado")
                        .isEqualTo(1));

        // 3. Nadie lo procesó todavía, claro: el consumidor sigue caído.
        assertThat(notificador.enviadas())
                .as("con el consumidor caído no se envía nada — pero el mensaje sigue existiendo")
                .isEmpty();
    }

    @Test
    @DisplayName("cuando el servicio vuelve, los avisos que esperaban se procesan solos")
    void alVolverElConsumidorSeProcesaLoAcumulado() {
        apagarConsumidor();

        String carolina = bearer(CAROLINA);
        int cuantos = 3;
        for (int i = 0; i < cuantos; i++) {
            Long tramite = crearTramite(carolina);
            cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                    .header("Authorization", carolina)
                    .exchange().expectStatus().isCreated();
        }

        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(mensajesEn(AmqpConfig.COLA))
                        .as("los tres avisos esperan acumulados")
                        .isEqualTo(cuantos));

        // El servicio de avisos vuelve. Nadie tiene que reenviar nada a mano: la cola tenía la
        // lista, que es exactamente lo que a Carolina le faltaba para saber "cuáles doscientos".
        encenderConsumidor();

        Awaitility.await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("al volver el consumidor, los avisos acumulados se procesan solos")
                        .hasSize(cuantos));

        assertThat(mensajesEn(AmqpConfig.COLA))
                .as("y la cola queda vacía: todo lo que esperaba se entregó")
                .isZero();
    }
}
