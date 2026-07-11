package cl.dgt.tramites.resiliencia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · El timeout dirigido. TESO tarda 3 s; el read timeout es de 800 ms. La aserción es DOBLE:
 * (a) el pago falla RÁPIDO (mucho antes de los 3 s) y (b) MIENTRAS ese pago está en vuelo, otro
 * endpoint —el listado, que no tiene nada que ver con pagos— responde igual de rápido. Ese es el
 * corazón: sin timeout, el hilo del pago es un rehén y la API entera se cuelga; con timeout, no.
 */
class E1_TimeoutIT extends BaseResilienciaIT {

    @Test
    @DisplayName("el pago falla rápido Y la API sigue viva mientras tanto")
    void fallaRapidoYLaApiSigueViva() throws Exception {
        tesoRespondeCon(3000);   // TESO cuelga 3 segundos
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramitePresentado(carolina);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            // (a) el pago lento, EN PARALELO
            Future<Long> msPago = pool.submit(() -> {
                long t0 = System.nanoTime();
                cliente().post().uri("/api/v1/tramites/" + tramite + "/pago")
                        .header("Authorization", carolina)
                        .exchange().expectStatus().isEqualTo(503);
                return (System.nanoTime() - t0) / 1_000_000;
            });

            // (b) mientras el pago está colgándose, el listado debe responder RÁPIDO
            long t1 = System.nanoTime();
            cliente().get().uri("/api/v1/tramites").header("Authorization", carolina)
                    .exchange().expectStatus().isOk();
            long msListado = (System.nanoTime() - t1) / 1_000_000;

            assertThat(msListado)
                    .as("el listado NO quedó preso detrás del pago colgado")
                    .isLessThan(2000);
            assertThat(msPago.get())
                    .as("el pago falló rápido (timeout), no esperó los 3 s de TESO")
                    .isLessThan(2000);
        } finally {
            pool.shutdownNow();
        }
    }
}
