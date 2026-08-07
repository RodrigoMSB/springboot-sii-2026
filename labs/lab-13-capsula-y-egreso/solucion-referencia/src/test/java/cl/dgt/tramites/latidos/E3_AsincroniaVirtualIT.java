package cl.dgt.tramites.latidos;

import cl.dgt.tramites.application.NotificadorService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · <strong>Asincronía sobre hilos virtuales (Java 25).</strong>
 *
 * <p>Tres afirmaciones, y la tercera es la que más gente falla:
 *
 * <ol>
 *   <li>La llamada <strong>vuelve enseguida</strong>: quien la hizo no espera al aviso.</li>
 *   <li>El aviso <strong>ocurre igual</strong>, en un <strong>hilo virtual</strong>.</li>
 *   <li>La <strong>autoinvocación no es asíncrona</strong>. Por tercera vez en el curso.</li>
 * </ol>
 *
 * <p>Se espera con {@link Awaitility}, nunca durmiendo: un test que duerme afirma «en mi máquina
 * tardó menos que esto», que no es una afirmación sobre el sistema. AU-05 lo vigila.
 */
@DisplayName("TODO_3 · las notificaciones salen del hilo de la petición, sobre hilos virtuales")
class E3_AsincroniaVirtualIT extends BaseLatidosIT {

    @Autowired
    NotificadorService notificador;

    @BeforeEach
    void vaciarLaBandeja() {
        notificador.limpiar();
    }

    @Test
    @DisplayName("notificar() vuelve enseguida y el aviso llega después, en un hilo VIRTUAL")
    void elAvisoSaleEnOtroHiloYNadieLoEspera() {
        assertThat(notificador.enviadas()).isEmpty();

        notificador.notificar("11111111-1", "su trámite fue recibido");

        // El aviso puede no estar todavía: eso es precisamente lo que significa asíncrono. Se
        // espera a que aparezca, sin fijar cuánto tarda.
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(notificador.enviadas()).hasSize(1));

        NotificadorService.Aviso aviso = notificador.enviadas().getFirst();

        assertThat(aviso.hiloVirtual())
                .as("debe correr en un hilo VIRTUAL: esta carga se pasa el tiempo esperando a otro "
                    + "servicio, y un hilo de plataforma bloqueado es un megabyte gastado en no "
                    + "hacer nada. Si esto es false, el executor no es de hilos virtuales — o "
                    + "@Async no actuó")
                .isTrue();

        assertThat(aviso.hilo())
                .as("y no en el hilo del test: si coincidieran, no hubo asincronía")
                .doesNotContain(Thread.currentThread().getName());
    }

    @Test
    @DisplayName("emitir un folio responde SIN esperar a que salga la notificación")
    void laPeticionNoEsperaAlCorreo() {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        long antes = System.nanoTime();
        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isCreated();
        long tardanzaMs = (System.nanoTime() - antes) / 1_000_000;

        // La cota es generosa a propósito: no se está midiendo lo rápido que es el servidor —eso
        // sería un test frágil— sino que la respuesta NO se queda esperando a un servicio externo.
        assertThat(tardanzaMs)
                .as("la respuesta no debe quedarse esperando al notificador")
                .isLessThan(5_000);

        // Y el aviso sale igual, después. Desacoplar no es perder.
        Awaitility.await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(notificador.enviadas())
                        .as("el aviso del folio debe llegar, aunque la petición ya haya respondido")
                        .anyMatch(a -> a.mensaje().contains("folio")));
    }

    @Test
    @DisplayName("LA TRAMPA DEL PROXY: la autoinvocación NO es asíncrona (tercera vez en el curso)")
    void laAutoinvocacionNoPasaPorElProxy() {
        String hiloDelTest = Thread.currentThread().getName();

        // notificarPorDentro() llama a notificar() con `this`. Esa llamada NO pasa por el proxy de
        // Spring, así que @Async no se aplica: corre aquí mismo, síncrono, y nadie avisa de nada.
        notificador.notificarPorDentro("11111111-1", "aviso por dentro");

        // Ya está: sin esperar nada. Si fuera asíncrono, esta aserción sería una carrera.
        assertThat(notificador.enviadas())
                .as("la autoinvocación se ejecutó de forma síncrona, así que el aviso YA está")
                .hasSize(1);

        NotificadorService.Aviso aviso = notificador.enviadas().getFirst();

        assertThat(aviso.hiloVirtual())
                .as("corrió en el hilo del llamador, no en uno virtual del executor")
                .isFalse();
        assertThat(aviso.hilo())
                .as("es EXACTAMENTE el hilo del test: el proxy no intervino. Es el mismo límite de "
                    + "@Transactional (Lab 06) y del aspecto de auditoría (Lab 09) — las "
                    + "anotaciones no son magia del compilador: son un objeto envolviendo a otro, "
                    + "y desde dentro del objeto envuelto el envoltorio no existe")
                .contains(hiloDelTest);
    }
}
