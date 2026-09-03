// Lo que sale por el endpoint: la cabecera del resumen y sus trámites.
// Tu equivalente: `dto/ConsolidadoContribuyente`, con `rut` y `razonSocial` en vez de
// `codigo` y `nombre`.
package cl.dgt.consolidado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenOficina(
        String codigo,
        String nombre,
        LocalDate desde,
        LocalDate hasta,
        List<TramiteDelResumen> tramites,
        BigDecimal totalDeclarado) {
}
// ^ Es un `record` y no una clase, y no es sólo por escribir menos: un record es INMUTABLE.
//   Una vez armado, nadie puede cambiarle un campo camino del JSON.
//
//   Y es una LISTA BLANCA: enumera lo que sale. Todo lo que no esté aquí, no sale — aunque la
//   entidad lo tenga. Ésa es la razón de que exista un DTO en vez de devolver la entidad: el día
//   que alguien añada una columna a `contribuyente`, no se publica sola.
