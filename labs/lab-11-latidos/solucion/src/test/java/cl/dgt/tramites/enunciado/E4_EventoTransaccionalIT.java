package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.EmisionService;
import cl.dgt.tramites.application.NotificadorService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_4 · <strong>El evento transaccional.</strong>
 *
 * <p>El test estrella del laboratorio, y la lección más cara de las cuatro:
 * <strong>avisar de algo que no ocurrió es peor que no avisar</strong>.
 *
 * <p>El que no recibe nada pregunta. El que recibe un folio que nunca existió lo anota, lo declara,
 * lo cita ante un fiscalizador — y el problema aparece meses después, cuando ya es de otro. Un
 * {@code @EventListener} normal manda el aviso al publicarse el evento, es decir <em>dentro</em> de
 * la transacción, cuando el folio todavía no es un hecho. Si esa transacción revierte, el correo ya
 * salió y no hay forma de desenviarlo.
 *
 * <p>{@code @TransactionalEventListener(AFTER_COMMIT)} mueve el listener al otro lado de la
 * frontera. Estos dos tests son las dos mitades de la misma afirmación: si hay commit, avisa; si
 * hay rollback, calla.
 */
@DisplayName("TODO_4 · el aviso sale solo si la transacción llegó a puerto")
class E4_EventoTransaccionalIT extends BaseLatidosIT {

    @Autowired
    NotificadorService notificador;

    @Autowired
    EmisionService emision;

    @Autowired
    PlatformTransactionManager gestorDeTransacciones;

    @BeforeEach
    void vaciarLaBandeja() {
        notificador.limpiar();
    }

    @Test
    @DisplayName("COMMIT: emitir un folio de verdad sí avisa al contribuyente")
    void siLaTransaccionConfirmaElAvisoSale() {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isCreated();

        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("el folio se emitió y se confirmó: el aviso DEBE salir")
                        .anyMatch(a -> a.mensaje().contains("folio")));
    }

    @Test
    @DisplayName("ROLLBACK: si la transacción revierte, el aviso NO sale — el folio nunca existió")
    void siLaTransaccionRevierteElAvisoNoSale() {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        TransactionTemplate transaccion = new TransactionTemplate(gestorDeTransacciones);

        // emitir() es @Transactional con propagación REQUIRED: se une a ESTA transacción en vez de
        // abrir la suya. Así, marcar el rollback aquí revierte también la emisión — que es
        // exactamente lo que pasa en producción cuando algo falla después de emitir: una validación
        // posterior, un CHECK de la base, una caída.
        try {
            transaccion.execute(estado -> {
                emision.emitir(tramite);
                estado.setRollbackOnly();
                return null;
            });
        } catch (RuntimeException esperado) {
            // Si el gestor decide señalar el rollback con excepción, es parte del escenario.
        }

        // La afirmación negativa necesita una ventana de tiempo: si el aviso fuera a salir, saldría
        // en este rato. Awaitility, no sleep (AU-05).
        Awaitility.await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("la transacción revirtió: ese folio NO existe. Avisar de algo que no "
                            + "ocurrió es peor que no avisar — con @EventListener normal este aviso "
                            + "habría salido igual, y el contribuyente tendría un folio fantasma")
                        .noneMatch(a -> a.mensaje().contains("folio")));
    }
}
