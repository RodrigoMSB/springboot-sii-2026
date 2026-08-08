package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.EmisionService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · <strong>Métricas de NEGOCIO.</strong>
 *
 * <p>La diferencia que mide este test: el CPU, la memoria y los hilos los publica Actuator solo,
 * sin que nadie escriba una línea. Lo que ninguna métrica de infraestructura sabe es
 * <em>cuántos folios emitió la DGT hoy</em>. Un servidor al 3 % de carga puede llevar dos horas
 * sin emitir uno solo porque un validador rechaza todo: la máquina, perfecta; el negocio, detenido.
 * El contador en cero lo grita; el CPU, no.
 *
 * <p>Se verifica en los DOS sitios donde tiene que estar, porque son dos cosas distintas:
 * el {@link MeterRegistry} (la métrica existe y sube) y el scrape de {@code /actuator/prometheus}
 * (la métrica <em>sale</em> de la aplicación). Una métrica que sube pero no se publica no la ve
 * nadie; una serie publicada que no se mueve no sirve para nada.
 */
@DisplayName("TODO_2 · las métricas de negocio existen, suben al emitir un folio y salen por Prometheus")
class E2_MetricasDeNegocioIT extends BaseTableroIT {

    /** Serie de Prometheus del contador: los puntos se vuelven guiones bajos y el contador gana `_total`. */
    private static final String SERIE_FOLIOS = "dgt_folios_emitidos_total";
    /** El Timer publica tres series; `_count` es la que dice cuántas emisiones se cronometraron. */
    private static final String SERIE_TARDANZA = "dgt_folios_emision_seconds_count";

    @Autowired
    MeterRegistry metricas;

    @Test
    @DisplayName("el contador y el timer están REGISTRADOS aunque nadie haya emitido nada todavía")
    void lasMetricasNacenRegistradasEnCero() {
        // Registradas desde el constructor del servicio, no la primera vez que suben. Una serie
        // AUSENTE no se distingue de «el scrape falló»; una serie en cero dice «vivo, sin emitir»,
        // que es justo la alerta que Carolina necesita. La ausencia es una duda, no un dato.
        assertThat(metricas.find(EmisionService.METRICA_FOLIOS).counters())
                .as("el contador de folios debe existir antes del primer folio")
                .isNotEmpty();
        assertThat(metricas.find(EmisionService.METRICA_TARDANZA).timer())
                .as("el timer de emisión debe existir antes de la primera emisión")
                .isNotNull();
    }

    @Test
    @DisplayName("emitir un folio incrementa el contador en 1 y cronometra la emisión")
    void emitirUnFolioMueveLasAgujas() {
        String carolina = bearer(CAROLINA);

        String antes = scrapePrometheus(carolina);
        double foliosAntes = serie(antes, SERIE_FOLIOS, "resultado=\"nuevo\"");
        double emisionesAntes = serie(antes, SERIE_TARDANZA, "dgt_folios_emision_seconds_count");

        Long tramite = crearTramite(carolina);
        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isCreated();

        String despues = scrapePrometheus(carolina);
        double foliosDespues = serie(despues, SERIE_FOLIOS, "resultado=\"nuevo\"");
        double emisionesDespues = serie(despues, SERIE_TARDANZA, "dgt_folios_emision_seconds_count");

        assertThat(foliosDespues)
                .as("un folio emitido = una unidad más en dgt_folios_emitidos_total{resultado=\"nuevo\"}")
                .isEqualTo(foliosAntes + 1);
        assertThat(emisionesDespues)
                .as("el timer debe haber cronometrado la emisión")
                .isEqualTo(emisionesAntes + 1);
    }

    @Test
    @DisplayName("el reintento idempotente (RN-05) cuenta aparte: reusado no es lo mismo que nuevo")
    void elReintentoNoSeConfundeConUnaEmisionNueva() {
        String carolina = bearer(CAROLINA);
        Long tramite = crearTramite(carolina);

        // Primera emisión: 201, folio nuevo.
        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isCreated();

        String antes = scrapePrometheus(carolina);
        double nuevosAntes = serie(antes, SERIE_FOLIOS, "resultado=\"nuevo\"");
        double reusadosAntes = serie(antes, SERIE_FOLIOS, "resultado=\"reusado\"");

        // Reintento sobre el MISMO trámite: 200, el mismo folio (RN-05).
        cliente().post().uri("/api/v1/tramites/" + tramite + "/folio")
                .header("Authorization", carolina)
                .exchange().expectStatus().isOk();

        String despues = scrapePrometheus(carolina);

        // La etiqueta `resultado` es lo que hace útil a esta métrica. Sin ella, un pico en el
        // contador no distingue «hoy se declaró mucho» de «un cliente está reintentando en bucle»,
        // que son una buena noticia y una incidencia.
        assertThat(serie(despues, SERIE_FOLIOS, "resultado=\"reusado\""))
                .as("el reintento suma en `reusado`")
                .isEqualTo(reusadosAntes + 1);
        assertThat(serie(despues, SERIE_FOLIOS, "resultado=\"nuevo\""))
                .as("y NO suma en `nuevo`: no se emitió un folio nuevo")
                .isEqualTo(nuevosAntes);
    }
}
