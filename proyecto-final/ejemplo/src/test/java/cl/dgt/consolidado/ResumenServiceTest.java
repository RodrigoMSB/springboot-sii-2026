// Test de servicio con el repositorio doblado: comprueba que el TOTAL suma bien.
// Tu equivalente: el mismo test sobre `ConsolidadoService`, con un RUT en vez de un código.
package cl.dgt.consolidado;

import cl.dgt.consolidado.dto.ResumenOficina;
import cl.dgt.consolidado.entities.Oficina;
import cl.dgt.consolidado.entities.Tramite;
import cl.dgt.consolidado.repositories.OficinaRepository;
import cl.dgt.consolidado.repositories.TramiteRepository;
import cl.dgt.consolidado.services.ContadorDeConsolidados;
import cl.dgt.consolidado.services.OficinaNoEncontradaException;
import cl.dgt.consolidado.services.ResumenService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumenServiceTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 12, 31);

    @Mock
    private OficinaRepository oficinas;

    @Mock
    private TramiteRepository tramites;

    private ResumenService servicioCon(List<Tramite> devueltos) {
        when(oficinas.findByCodigo("SCL-CEN"))
                .thenReturn(Optional.of(new Oficina("SCL-CEN", "Santiago Centro")));
        when(tramites.delPeriodo(eq("SCL-CEN"), any(), any())).thenReturn(devueltos);

        return new ResumenService(oficinas, tramites,
                new ContadorDeConsolidados(new SimpleMeterRegistry()));
    }
    // ^ El contador NO se dobla: se le pasa un `SimpleMeterRegistry`, que es el registro de
    //   verdad de Micrometer sin nada detrás. Doblarlo obligaría a un `when` más que no prueba
    //   nada; así el contador se incrementa de verdad y al test le da igual.

    @Test
    void elTotalSumaLosMontosDelPeriodo() {
        ResumenService servicio = servicioCon(List.of(
                tramite(1L, "F29", "PAGADO",    new BigDecimal("1200000.00")),
                tramite(2L, "F22", "PENDIENTE", new BigDecimal("3400000.00"))));

        ResumenOficina resumen = servicio.delPeriodo("SCL-CEN", DESDE, HASTA);

        assertEquals(0, new BigDecimal("4600000.00").compareTo(resumen.totalDeclarado()));
        assertEquals(2, resumen.tramites().size());
    }
    // ^ DOS COSAS QUE MIRAR:
    //
    //   1. Se suman los DOS trámites, incluido el PENDIENTE. El total no filtra por estado, y
    //      este test es lo que lo deja escrito: si alguien mañana añade un `if` por estado, se
    //      pone rojo.
    //
    //   2. `compareTo` y no `equals` para comparar `BigDecimal`. `equals` compara también la
    //      ESCALA, así que `4600000.00` y `4600000.0` no son iguales para él aunque valgan lo
    //      mismo. Es la trampa clásica de BigDecimal en un test.

    @Test
    void unaOficinaQueNoExisteLanzaLaExcepcionDel404() {
        when(oficinas.findByCodigo("NO-EXISTE")).thenReturn(Optional.empty());

        ResumenService servicio = new ResumenService(oficinas, tramites,
                new ContadorDeConsolidados(new SimpleMeterRegistry()));

        assertThrows(OficinaNoEncontradaException.class,
                () -> servicio.delPeriodo("NO-EXISTE", DESDE, HASTA));
    }

    private static Tramite tramite(Long id, String tipo, String estado, BigDecimal monto) {
        return new Tramite(id, tipo, estado, LocalDate.of(2026, 3, 1), monto, "SCL-CEN");
    }
    // ^ Este constructor SÓLO existe para los tests — ver la nota en la entidad. La alternativa
    //   sería construir la entidad con reflexión o levantar la base, y las dos son peores.
}
