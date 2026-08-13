package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.ReporteService;
import cl.dgt.tramites.application.ReporteService.TotalPorPeriodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** TODO_4 · Un reporte agregado con JdbcClient, sin cargar entidades. */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BasePersistenciaIT.class)
class E4_ReporteJdbcIT {

    @Autowired ReporteService reportes;

    @Test
    @DisplayName("el total declarado por período suma las líneas en SQL, no en Java")
    void totalPorPeriodo() {
        List<TotalPorPeriodo> totales = reportes.totalDeclaradoPorPeriodo();

        assertThat(totales).as("hay períodos con F29 en la semilla").isNotEmpty();
        // 2026-04: líneas 1.250.000 y -340.000  => 910.000
        assertThat(totales)
                .anySatisfy(t -> {
                    assertThat(t.periodo()).isEqualTo("2026-04");
                    assertThat(t.total()).isEqualTo(910_000L);
                });
    }
}
