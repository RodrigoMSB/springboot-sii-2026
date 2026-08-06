package cl.dgt.tramites.tablero;

import cl.dgt.tramites.application.ReporteService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_4 · <strong>Caché con medición e invalidación.</strong>
 *
 * <p>Dos afirmaciones, y ninguna se puede sostener mirando el código:
 *
 * <ol>
 *   <li>La segunda llamada idéntica <strong>no baja a la base</strong>. Se prueba con las
 *       estadísticas de Caffeine, que son la única evidencia dura de que el caché sirvió: un
 *       {@code @Cacheable} escrito no es un caché funcionando —basta una autoinvocación, un
 *       proveedor mal configurado o un nombre de caché con una errata para que la anotación no
 *       haga nada, en silencio y sin un solo error—.</li>
 *   <li>Cuando el dato cambia, el caché <strong>se invalida</strong>. Sin esto, lo que tenemos no
 *       es un caché: es un mentiroso con buena memoria, que responde rápido, con seguridad y con
 *       el dato de antes.</li>
 * </ol>
 *
 * <p>Se mide por DELTAS, no por valores absolutos: las estadísticas de Caffeine son acumulativas
 * desde que nació el bean y otros tests de esta misma clase ya las movieron. Un test que asume
 * «hitCount == 1» funciona el día que corre solo y se cae el día que corre acompañado.
 */
@DisplayName("TODO_4 · el caché ahorra viajes a la base, y se invalida cuando el dato cambia")
class E4_CacheMedidoIT extends BaseTableroIT {

    /** El reporte caro: JOIN + GROUP BY sobre todo el histórico de declaraciones. */
    private static final String REPORTE = "/api/v1/reportes/totales-por-periodo";

    /** Un trámite de la semilla que YA tiene F29 (V2: los trámites 1..4 son DECLARACION_F29). */
    private static final long TRAMITE_CON_F29 = 1L;

    /**
     * {@code required = false} para que el starter falle con un mensaje ÚTIL en vez de reventar al
     * construir el test. Sin {@code @EnableCaching} no hay {@code CacheManager} en el contexto, y
     * «no se pudo inyectar la dependencia» no le dice a nadie qué hacer.
     */
    @Autowired(required = false)
    CacheManager cacheManager;

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> cacheNativo() {
        assertThat(cacheManager)
                .as("no hay CacheManager en el contexto: falta @EnableCaching (TODO_4)")
                .isNotNull();

        org.springframework.cache.Cache cache = cacheManager.getCache(ReporteService.CACHE_TOTALES);
        assertThat(cache)
                .as("no existe el caché '%s': ¿lo declara el CacheManager?", ReporteService.CACHE_TOTALES)
                .isNotNull();

        assertThat(cache)
                .as("el caché existe pero NO es Caffeine: el mapa por omisión de Boot no expira, no "
                    + "se acota y no lleva estadísticas — sin stats no hay hit-rate que medir, y sin "
                    + "hit-rate este TODO sería un acto de fe")
                .isInstanceOf(CaffeineCache.class);

        return ((CaffeineCache) cache).getNativeCache();
    }

    @BeforeEach
    void vaciarElCache() {
        // Vaciar NO reinicia las estadísticas (son acumulativas). Solo garantiza que la próxima
        // llamada sea un fallo de caché, que es el punto de partida que cada prueba necesita.
        cacheNativo().invalidateAll();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> pedirReporte(String bearer) {
        return cliente().get().uri(REPORTE)
                .header("Authorization", bearer)
                .exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
    }

    @Test
    @DisplayName("la segunda llamada idéntica se sirve de memoria: un acierto, sin viaje a la base")
    void laSegundaLlamadaNoGolpeaLaBase() {
        String carolina = bearer(CAROLINA);
        CacheStats antes = cacheNativo().stats();

        List<Map<String, Object>> primera = pedirReporte(carolina);
        List<Map<String, Object>> segunda = pedirReporte(carolina);

        CacheStats despues = cacheNativo().stats();

        assertThat(despues.missCount() - antes.missCount())
                .as("la PRIMERA llamada sí baja a la base: un fallo de caché, exactamente uno")
                .isEqualTo(1);
        assertThat(despues.hitCount() - antes.hitCount())
                .as("la SEGUNDA se sirve de memoria: un acierto. Si esto es 0, el @Cacheable no "
                    + "está actuando (¿autoinvocación? ¿nombre de caché distinto? ¿falta @EnableCaching?)")
                .isEqualTo(1);

        // Y el caché no puede cambiar la respuesta: servir rápido algo distinto sería peor que ser lento.
        assertThat(segunda).isEqualTo(primera);
    }

    @Test
    @DisplayName("declarar una línea invalida el caché: la siguiente lectura vuelve a la base y trae el dato nuevo")
    void declararUnaLineaInvalidaElCache() {
        String carolina = bearer(CAROLINA);

        List<Map<String, Object>> antesDeDeclarar = pedirReporte(carolina);
        pedirReporte(carolina);                       // deja el valor caliente en memoria
        CacheStats trasCalentar = cacheNativo().stats();

        // La escritura. DeclaracionService la marca con @CacheEvict(allEntries = true): quien
        // escribe el dato es el responsable de invalidar la copia.
        long monto = 777_000L;
        cliente().post().uri("/api/v1/tramites/" + TRAMITE_CON_F29 + "/f29/lineas")
                .header("Authorization", carolina)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("codigo", "538", "monto", monto))
                .exchange().expectStatus().isCreated();

        List<Map<String, Object>> despuesDeDeclarar = pedirReporte(carolina);
        CacheStats trasReleer = cacheNativo().stats();

        assertThat(trasReleer.missCount() - trasCalentar.missCount())
                .as("tras el @CacheEvict, la siguiente lectura DEBE volver a la base: otro fallo de caché")
                .isEqualTo(1);

        // La prueba de que la invalidación sirvió para algo: el número cambió. Sin evict, esta
        // aserción fallaría sirviendo el total viejo — rápido, seguro y equivocado.
        assertThat(despuesDeDeclarar)
                .as("el reporte debe reflejar la línea recién declarada")
                .isNotEqualTo(antesDeDeclarar);

        long totalAntes = totalDelPeriodo(antesDeDeclarar, "2026-04");
        long totalDespues = totalDelPeriodo(despuesDeDeclarar, "2026-04");
        assertThat(totalDespues - totalAntes)
                .as("el total del período 2026-04 debe subir exactamente en el monto declarado")
                .isEqualTo(monto);
    }

    private static long totalDelPeriodo(List<Map<String, Object>> reporte, String periodo) {
        return reporte.stream()
                .filter(fila -> periodo.equals(fila.get("periodo")))
                .mapToLong(fila -> ((Number) fila.get("total")).longValue())
                .findFirst()
                .orElseThrow(() -> new AssertionError("el reporte no trae el período " + periodo));
    }
}
