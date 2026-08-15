package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AU-03b sigue de guardia.
 *
 * <p>El guardián lo instaló el alumno en el Lab 3.5, y aquí sigue vigilando: un guardián que
 * deja de correr en el lab siguiente no era un guardián, era un ejercicio.
 *
 * <p>Vive en su propio archivo por lo mismo que {@link ReglasDelApostrofe}: las siete reglas de
 * la casa y sus meta-tests viajan desde el tronco por toda la cadena, y sumarles una octava
 * obligaría a tocar todos los laboratorios a la vez.
 *
 * <p><strong>Y este lab es justamente donde importa comprobarlo</strong>, porque el Lab 04
 * escribe SQL a propósito: {@code ReporteService} usa {@code JdbcClient}. AU-03b tiene que
 * dejarlo pasar —es SQL con parámetros y recursos gestionados— y seguir cazando el JDBC a pelo.
 */
class GuardianJdbcTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    private static final JavaClasses CON_FIXTURE = new ClassFileImporter().importPackages(
            "cl.dgt.tramites.domain", "cl.dgt.tramites.web",
            "cl.dgt.tramites.application", "cl.dgt.tramites.infrastructure",
            "cl.dgt.tramites.arquitectura.fixtures.violaciones.jdbc");

    @Test
    @DisplayName("AU-03b pasa: el JdbcClient del reporte no es JDBC crudo")
    void au03bPasa() {
        ReglasDelApostrofe.AU_03B.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-03b sigue mordiendo a quien abre la conexión a mano")
    void au03bMuerde() {
        assertThatThrownBy(() -> ReglasDelApostrofe.AU_03B.check(CON_FIXTURE))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU03B_ClaseQueHablaJdbcCrudo");
    }
}
