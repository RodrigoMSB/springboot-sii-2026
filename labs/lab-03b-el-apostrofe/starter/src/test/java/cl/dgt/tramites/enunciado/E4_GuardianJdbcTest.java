package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.arquitectura.ReglasDelApostrofe;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TODO_4 · El muerto enterrado y el guardián instalado.
 *
 * <p>Las dos mitades se exigen por separado, y hacen falta las dos. Que una regla pase sobre tu
 * código no prueba nada: una regla mal escrita también pasa. Por eso la segunda mitad la
 * enfrenta a una clase que la viola a propósito y exige que FALLE.
 */
@DisplayName("TODO_4 · AU-03b: nadie habla JDBC crudo, y el guardián lo demuestra")
class E4_GuardianJdbcTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    private static final JavaClasses CON_FIXTURE = new ClassFileImporter().importPackages(
            "cl.dgt.tramites.domain", "cl.dgt.tramites.web",
            "cl.dgt.tramites.application", "cl.dgt.tramites.infrastructure",
            "cl.dgt.tramites.arquitectura.fixtures.violaciones.jdbc");

    @Test
    @DisplayName("El DAO heredado ya no existe")
    void elMuertoEstaEnterrado() {
        assertThat(PRODUCCION.stream().map(c -> c.getName()))
                .as("ReporteInternoLegacyDao sigue en el proyecto: el laboratorio no termina "
                    + "hasta que el muerto está enterrado (TODO_4)")
                .noneMatch(nombre -> nombre.contains("ReporteInternoLegacyDao"));
    }

    @Test
    @DisplayName("AU-03b pasa sobre el código de producción")
    void au03bPasa() {
        ReglasDelApostrofe.AU_03B.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-03b MUERDE a una clase que abre la conexión y arma un Statement")
    void au03bMuerde() {
        assertThatThrownBy(() -> ReglasDelApostrofe.AU_03B.check(CON_FIXTURE))
                .as("el guardián todavía es el cascarón tautológico: no vigila nada (TODO_4)")
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU03B_ClaseQueHablaJdbcCrudo");
    }
}
