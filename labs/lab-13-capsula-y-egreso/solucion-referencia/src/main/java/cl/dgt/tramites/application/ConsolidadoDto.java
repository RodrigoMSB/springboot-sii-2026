package cl.dgt.tramites.application;

import java.util.List;

/**
 * El consolidado que viaja al fiscalizador.
 *
 * <p><strong>Lista blanca, como manda el Lab 02.</strong> Aquí no aparece el
 * {@code puntajeRiesgoInterno} del contribuyente ni el {@code claveHash} de nadie: el DTO enumera lo
 * que sale, no excluye lo que no debe salir. Un DTO que se construye quitando campos vuelve a
 * filtrarlos el día que alguien agrega una columna.
 *
 * @param rut             a quién corresponde el consolidado
 * @param razonSocial     su nombre, que es lo que el fiscalizador lee
 * @param periodo         el período consolidado (obligatorio: ver decisión 5 de {@link ConsolidadoService})
 * @param tramites        el detalle, con el estado de cada uno y su folio si ya se emitió
 * @param totalDeclarado  la suma de las líneas del F29 de ese período
 */
public record ConsolidadoDto(
        String rut,
        String razonSocial,
        String periodo,
        List<ConsolidadoService.TramiteDelConsolidado> tramites,
        long totalDeclarado) {
}
