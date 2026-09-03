// Cada trámite dentro del resumen.
// Tu equivalente: `dto/TramiteDelConsolidado`. Los campos son EXACTAMENTE los mismos.
package cl.dgt.consolidado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TramiteDelResumen(
        Long id,
        String tipo,
        String estado,
        LocalDate fecha,
        BigDecimal montoDeclarado) {
}
// ^ `montoDeclarado` se llama distinto que en la entidad, donde el campo es `monto`. No es un
//   descuido: el nombre de fuera es del contrato de la API y el de dentro es del modelo, y
//   conviene que puedan cambiar por separado. La traducción se hace en el servicio.
