package cl.dgt.tramites.application;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reportes agregados. Aquí NO hay entidades: {@link JdbcClient} lee filas y las suma en
 * SQL, sin cargar un solo {@code Tramite} ni sus árboles.
 *
 * <p>La lección: no todo lo que lee la base merece el peaje del ORM. Un total por período
 * es una pregunta de columnas y sumas — el ORM cargaría objetos que nadie va a usar. Para
 * escribir y navegar el dominio, JPA. Para reportar, a veces, SQL directo.
 *
 * <p><strong>Y por qué esta consulta merece caché (M12).</strong> Cumple las tres condiciones, y
 * hay que exigir las tres antes de cachear nada:
 *
 * <ol>
 *   <li><strong>Es cara.</strong> {@code JOIN} de dos tablas + {@code GROUP BY} sobre todo el
 *       histórico. Crece con los años de declaraciones, no con el tráfico: el día que la DGT lleve
 *       diez años operando, esta consulta recorre diez años de líneas para responder lo mismo.</li>
 *   <li><strong>Se lee mucho más de lo que cambia.</strong> El tablero de Carolina la pide cada
 *       vez que alguien lo abre; los datos se mueven cuando se declara una línea nueva.</li>
 *   <li><strong>Tolera estar un poco desactualizada</strong> — y ese «un poco» está declarado,
 *       no supuesto: como mucho, el TTL de {@link cl.dgt.tramites.config.CacheConfig}.</li>
 * </ol>
 *
 * <p>Si una consulta falla cualquiera de las tres, cachearla es cambiar un problema de rendimiento
 * por uno de correctitud. Y los de correctitud se pagan más caros: el lento se nota, el incorrecto
 * no.
 */
@Service
public class ReporteService {

    /**
     * Nombre del caché. Constante y no literal suelto: lo nombran {@code @Cacheable}, el
     * {@code @CacheEvict} de {@link DeclaracionService}, el {@code CacheManager} y los tests. Cuatro
     * sitios con el mismo string escrito a mano es una errata esperando su turno — y una errata ahí
     * no rompe nada: crea un SEGUNDO caché, silencioso, que nadie invalida jamás.
     */
    public static final String CACHE_TOTALES = "totalesPorPeriodo";

    private final JdbcClient jdbc;

    public ReporteService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Total declarado (suma de líneas del F29) por período, ordenado por período.
     *
     * <p>La primera llamada baja a la base; las siguientes se sirven de memoria hasta que alguien
     * declare una línea nueva ({@code @CacheEvict}) o venza el TTL.
     *
     * <p><strong>Cuidado con el proxy</strong> —la misma trampa del Lab 09 y de
     * {@code @Transactional}—: {@code @Cacheable} lo aplica un proxy que envuelve al bean. Si otro
     * método de ESTA clase llamara a {@code totalDeclaradoPorPeriodo()} directamente, la llamada
     * no pasaría por el proxy y el caché no se usaría, sin un solo aviso. Por eso la invalidación
     * vive en OTRO bean.
     */
    // TODO_4 — Cachea este reporte. Es la consulta cara del lab: JOIN + GROUP BY sobre TODO el
    //          histórico de declaraciones. Crece con los años, no con el tráfico.
    //
    //          Exígele las tres condiciones antes de cachear nada (y si falla una, no caches):
    //            1. es cara;
    //            2. se lee mucho más de lo que cambia;
    //            3. tolera estar un poco desactualizada — y ese "un poco" se DECLARA (el TTL),
    //               no se supone.
    //
    //          Marcador de este TODO: {{TODO_4}}. Aquí no va un throw: el método FUNCIONA, solo
    //          que baja a la base cada vez. Un caché ausente es lento; uno mal invalidado miente.
    //          Por eso este TODO se mide con las estadísticas de Caffeine, no leyendo el código.
    //
    // Pista 2: @Cacheable(CACHE_TOTALES) sobre este método, y el proveedor Caffeine declarado en
    //          una @Configuration con @EnableCaching. Sin @EnableCaching la anotación no hace
    //          NADA, y no avisa.
    @Transactional(readOnly = true)
    public List<TotalPorPeriodo> totalDeclaradoPorPeriodo() {
        return jdbc.sql("""
                SELECT f.periodo AS periodo, SUM(l.monto) AS total
                  FROM formulario29 f
                  JOIN linea_f29 l ON l.formulario29_id = f.id
                 GROUP BY f.periodo
                 ORDER BY f.periodo
                """)
                .query(TotalPorPeriodo.class)
                .list();
    }

    /** Un dato, no una entidad. */
    public record TotalPorPeriodo(String periodo, long total) {}
}
