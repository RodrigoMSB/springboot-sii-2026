package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * El consolidado que pidió Carolina: los trámites de un contribuyente, su estado, y el total
 * declarado del período.
 *
 * <p><strong>Esta es UNA solución, no LA solución.</strong> Lo que sigue son las decisiones que
 * tomó quien escribió esta referencia, cada una con su porqué. Si tú decidiste distinto y puedes
 * defenderlo, tu entrega puede estar igual de bien — o mejor. Lo que no se acepta es no haber
 * decidido.
 *
 * <h2>Decisiones declaradas</h2>
 *
 * <p><strong>1 · SQL agregado, no el ORM.</strong> Es una pregunta de columnas y sumas sobre varias
 * tablas; cargar el árbol de entidades traería objetos que nadie va a usar y reabriría el N+1 del
 * Lab 05. Misma doctrina que {@link ReporteService}. El precio: hay SQL a mano que mantener.
 *
 * <p><strong>2 · Dos consultas, no una.</strong> Se podría resolver con un solo {@code JOIN} y
 * agrupar en memoria. Se eligieron dos —el detalle y el total— porque el {@code SUM} sobre un
 * producto cartesiano de trámites × líneas daría un total inflado, y arreglarlo con
 * {@code DISTINCT} anidados hace la consulta ilegible. Dos consultas simples le ganan a una
 * ingeniosa: la ingeniosa se rompe cuando alguien le agrega una tabla.
 *
 * <p><strong>3 · El contribuyente inexistente es 404, no una lista vacía.</strong> El brief no lo
 * decía. Una lista vacía es una respuesta legítima a «este contribuyente no tiene trámites»; para
 * «este contribuyente no existe» es una mentira cortés. El fiscalizador que teclea mal un RUT tiene
 * que enterarse.
 *
 * <p><strong>4 · Sin paginación.</strong> El brief tampoco lo decía. Un contribuyente tiene decenas
 * de trámites, no millones: paginar aquí sería complejidad sin problema que resolver. La decisión se
 * revisa el día que alguien mida un consolidado lento — y el Lab 05 dejó el contador de consultas
 * para medirlo.
 *
 * <p><strong>5 · El período es obligatorio.</strong> «El total declarado del período» exige saber
 * de qué período se habla. Un default silencioso —«el mes actual»— haría que el batch nocturno del
 * día 1 consolidara el mes equivocado a las 00:05. Que lo diga quien llama.
 */
@Service
public class ConsolidadoService {

    private final JdbcClient jdbc;

    public ConsolidadoService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public ConsolidadoDto consolidar(String rut, String periodo) {
        Identidad identidad = jdbc.sql("SELECT id, razon_social AS razonSocial FROM contribuyente WHERE rut = :rut")
                .param("rut", rut)
                .query(Identidad.class)
                .optional()
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rut));

        List<TramiteDelConsolidado> tramites = jdbc.sql("""
                SELECT t.id            AS tramiteId,
                       t.tipo          AS tipo,
                       t.estado        AS estado,
                       f.numero        AS folio
                  FROM tramite t
                  LEFT JOIN folio f ON f.tramite_id = t.id
                 WHERE t.contribuyente_id = :id
                 ORDER BY t.id
                """)
                .param("id", identidad.id())
                .query(TramiteDelConsolidado.class)
                .list();

        // Consulta aparte, y a propósito: sumar en el mismo JOIN que el detalle multiplicaría las
        // líneas por los trámites y daría un total inflado. Ver la decisión 2 del Javadoc.
        long total = jdbc.sql("""
                SELECT COALESCE(SUM(l.monto), 0)
                  FROM tramite t
                  JOIN formulario29 f29 ON f29.tramite_id = t.id
                  JOIN linea_f29   l   ON l.formulario29_id = f29.id
                 WHERE t.contribuyente_id = :id
                   AND f29.periodo = :periodo
                """)
                .param("id", identidad.id())
                .param("periodo", periodo)
                .query(Long.class)
                .single();

        return new ConsolidadoDto(rut, identidad.razonSocial(), periodo, tramites, total);
    }

    /** Solo para resolver el RUT a su id. No sale de aquí. */
    public record Identidad(Long id, String razonSocial) {}

    /** Un trámite dentro del consolidado. {@code folio} es null si todavía no se emitió. */
    public record TramiteDelConsolidado(Long tramiteId, String tipo, String estado, Long folio) {}
}
