package cl.dgt.tramites.application.evento;

/**
 * El aviso que viaja por la cola.
 *
 * <p><strong>No es la entidad, y no es el evento de Spring.</strong> Es un mensaje: un contrato
 * entre dos procesos que pueden estar en máquinas distintas, escritos por equipos distintos y
 * desplegados en momentos distintos. Por eso lleva datos planos y ningún comportamiento — lo que
 * cruza un límite de proceso tiene que poder serializarse y sobrevivir a que el otro lado vaya una
 * versión por detrás.
 *
 * @param avisoId    la <strong>clave de idempotencia</strong>. Identifica el HECHO («se emitió el
 *                   folio N»), no la entrega: si el broker entrega este mensaje dos veces, las dos
 *                   copias traen el mismo {@code avisoId} y el consumidor descarta la segunda.
 *                   Deliberadamente NO es un identificador aleatorio por mensaje: eso haría único
 *                   cada reenvío y la deduplicación no serviría de nada.
 * @param tramiteId  el trámite al que se le emitió folio
 * @param numero     el número de folio
 * @param rut        a quién avisar
 */
public record AvisoDeFolio(String avisoId, Long tramiteId, long numero, String rut) {

    /** Construye el aviso derivando la clave del hecho: un folio, un aviso. */
    public static AvisoDeFolio de(FolioEmitido evento) {
        return new AvisoDeFolio("folio-" + evento.numero(), evento.tramiteId(), evento.numero(), evento.rut());
    }
}
