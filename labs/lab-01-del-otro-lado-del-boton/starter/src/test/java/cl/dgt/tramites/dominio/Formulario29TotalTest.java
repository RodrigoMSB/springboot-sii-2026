package cl.dgt.tramites.dominio;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.Formulario29;
import cl.dgt.tramites.domain.entity.LineaF29;
import cl.dgt.tramites.domain.entity.Tramite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** RN-06: el total del F29 es la suma de sus líneas. Derivado, nunca persistido. */
class Formulario29TotalTest {

    private Formulario29 formularioVacio() {
        Tramite tramite = new Tramite(new Contribuyente("11111111-1", "Valentina Rojas", 12), "DECLARACION_F29");
        return new Formulario29(tramite, "2026-05");
    }

    @Test
    @DisplayName("Un F29 sin líneas totaliza cero")
    void sinLineasTotalizaCero() {
        assertThat(formularioVacio().total()).isZero();
    }

    @Test
    @DisplayName("El total suma las líneas, incluidos los créditos (montos negativos)")
    void sumaDebitosYCreditos() {
        Formulario29 f29 = formularioVacio();
        f29.agregarLinea(new LineaF29(f29, "538", 2_100_000L));
        f29.agregarLinea(new LineaF29(f29, "511", -560_000L));
        f29.agregarLinea(new LineaF29(f29, "062", 85_000L));

        assertThat(f29.total()).isEqualTo(1_625_000L);
    }

    @Test
    @DisplayName("El total se recalcula: no hay columna donde escribir una mentira")
    void elTotalSiempreSigueALasLineas() {
        Formulario29 f29 = formularioVacio();
        f29.agregarLinea(new LineaF29(f29, "538", 1_000_000L));
        assertThat(f29.total()).isEqualTo(1_000_000L);

        f29.agregarLinea(new LineaF29(f29, "511", -250_000L));
        assertThat(f29.total())
                .as("RN-06 no puede violarse: el total no se guarda, se deriva")
                .isEqualTo(750_000L);
    }
}
