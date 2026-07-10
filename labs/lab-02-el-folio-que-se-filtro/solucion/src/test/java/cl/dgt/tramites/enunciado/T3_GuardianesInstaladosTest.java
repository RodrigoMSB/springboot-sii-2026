package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.arquitectura.ReglasDeLaCasa;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TODO_3 · Los guardianes que instalaste MUERDEN.
 *
 * <p>Un guardián sin prueba de que muerde es un adorno. Este test exige, de AU-01 y AU-02,
 * las dos mitades del contrato:
 *
 * <ol>
 *   <li><strong>Pasan sobre el código de producción</strong> — si aún devuelves la entidad
 *       en algún controlador (el crimen sin arreglar), esto falla: el guardián te cazaría a
 *       ti. Instalar AU-02 y no tapar la filtración es imposible; esa es la gracia.</li>
 *   <li><strong>Fallan sobre el fixture</strong> — la clase que viola a propósito. Si tu
 *       regla no la caza, no protege nada.</li>
 * </ol>
 *
 * <p>Ojo con la trampa que el spike S-1 midió: una regla escrita sobre
 * {@code haveRawReturnType} PASA en verde mientras la entidad viaja dentro de un
 * {@code ResponseEntity<...>}. AU-02 debe razonar sobre DEPENDENCIAS
 * ({@code dependOnClassesThat}), que sí leen el genérico del bytecode.
 */
class T3_GuardianesInstaladosTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    private static final JavaClasses CON_FIXTURE_AU01 = new ClassFileImporter()
            .importPackages("cl.dgt.tramites.domain", "cl.dgt.tramites.arquitectura.fixtures.violaciones.web");

    private static final JavaClasses CON_FIXTURE_AU02 = new ClassFileImporter()
            .importPackages("cl.dgt.tramites.domain", "cl.dgt.tramites.arquitectura.fixtures.violaciones.au02");

    @Test
    @DisplayName("AU-01 pasa sobre producción y muerde a su fixture")
    void au01() {
        assertThatCode(() -> ReglasDeLaCasa.AU_01.check(PRODUCCION))
                .as("AU-01 no debe encontrar violaciones en el código de producción")
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> ReglasDeLaCasa.AU_01.check(CON_FIXTURE_AU01))
                .as("AU-01 debe cazar a la clase de web que toca la entidad")
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU01_WebQueTocaLaEntidad");
    }

    @Test
    @DisplayName("AU-02 pasa sobre producción y muerde al fixture del genérico")
    void au02() {
        assertThatCode(() -> ReglasDeLaCasa.AU_02.check(PRODUCCION))
                .as("Si el crimen sigue en pie (controlador que devuelve la entidad), esto falla")
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> ReglasDeLaCasa.AU_02.check(CON_FIXTURE_AU02))
                .as("AU-02 debe cazar la entidad escondida en el genérico del retorno")
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU02_ControladorQueFiltraEntidad")
                .hasMessageContaining("Contribuyente");
    }
}
