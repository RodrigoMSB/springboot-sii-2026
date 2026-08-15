package cl.dgt.tramites.concurrencia;

import cl.dgt.tramites.PostgresEmbebido;
import cl.dgt.tramites.application.EmisionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntConsumer;

/**
 * Config compartida de los tests de concurrencia: un PostgreSQL real (embebido) y el
 * ARNÉS que dispara N hilos a la vez. El arnés NO usa {@code Thread.sleep} —AU-05 lo prohíbe,
 * y con razón: un test que "espera un poquito" es un test que falla el martes—. Sincroniza con
 * un {@link CountDownLatch}: todos los hilos se preparan, esperan el disparo, y arrancan en el
 * mismo instante. Así la superposición es máxima y la carrera, reproducible.
 */
@TestConfiguration(proxyBeanMethods = false)
class BaseConcurrenciaIT {

    /**
     * El {@code DataSource} del PostgreSQL embebido, arrancado una sola vez por JVM. Antes esto
     * era un contenedor con {@code @ServiceConnection}; el motor es el mismo, lo que cambia es
     * que llega como dependencia Maven y no como imagen Docker (SPEC-022).
     */
    @Bean
    DataSource dataSource() {
        return PostgresEmbebido.nuevoDataSource();
    }

    /** Emite dentro de una transacción y REVIENTA después: prueba el rollback (E4). */
    @Bean
    EmisorQueFallaDespues emisorQueFallaDespues(EmisionService emision) {
        return new EmisorQueFallaDespues(emision);
    }

    /**
     * Lanza {@code hilos} tareas simultáneas y espera a que todas terminen. La `tarea` recibe el
     * índice del hilo (0..hilos-1). Cero `sleep`: la coordinación es por latches.
     */
    static void enParalelo(int hilos, IntConsumer tarea) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(hilos);
        CountDownLatch listos = new CountDownLatch(hilos);
        CountDownLatch partida = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(hilos);
        try {
            for (int i = 0; i < hilos; i++) {
                final int idx = i;
                pool.submit(() -> {
                    listos.countDown();
                    try {
                        partida.await();      // todos esperan el disparo — sin dormir
                        tarea.accept(idx);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        fin.countDown();
                    }
                });
            }
            listos.await();       // todos los hilos listos en la línea de partida
            partida.countDown();  // ¡ya!
            fin.await();          // esperamos a que todos crucen la meta
        } finally {
            pool.shutdownNow();
        }
    }

    /** Componente transaccional de apoyo para E4 (definido como @Bean para que Spring lo proxee). */
    static class EmisorQueFallaDespues {
        private final EmisionService emision;

        EmisorQueFallaDespues(EmisionService emision) {
            this.emision = emision;
        }

        @Transactional
        public void emitirYExplotar(Long tramiteId) {
            emision.emitir(tramiteId);   // se une a ESTA transacción (propagación REQUIRED)
            throw new IllegalStateException("boom: algo falló DESPUÉS de emitir el folio");
        }
    }
}
