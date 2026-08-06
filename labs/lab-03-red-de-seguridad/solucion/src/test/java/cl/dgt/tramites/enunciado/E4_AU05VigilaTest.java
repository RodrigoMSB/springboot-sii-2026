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
 * AU-05 · Ningún test duerme con {@code Thread.sleep}. Ya viene instalada; este meta-test
 * prueba que muerde. Un {@code sleep} en un test es una apuesta: pasa en tu máquina y falla
 * en el CI. Cuando lleguen los labs asíncronos, la alternativa es Awaitility (ya en el
 * classpath): esperar una condición, no un número de milisegundos.
 */
class E4_AU05VigilaTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    private static final JavaClasses CON_FIXTURE = new ClassFileImporter()
            .importPackages("cl.dgt.tramites.arquitectura.fixtures.violaciones.au05");

    // Todo el proyecto —producción Y tus tests— MENOS los fixtures (que duermen a
    // propósito). Aquí es donde AU-05 vigila TU código: si pones un Thread.sleep en un
    // test tuyo, esto se pone rojo.
    private static final JavaClasses TODO_MENOS_FIXTURES = new ClassFileImporter()
            .withImportOption(location -> !location.contains("fixtures"))
            .importPackages("cl.dgt.tramites");

    @Test
    @DisplayName("AU-05 pasa: el código de producción no llama a Thread.sleep")
    void au05PasaSobreElProyecto() {
        assertThatCode(() -> ReglasDeLaCasa.AU_05.check(PRODUCCION))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("AU-05 muerde: caza a la clase que duerme")
    void au05MuerdeSuFixture() {
        assertThatThrownBy(() -> ReglasDeLaCasa.AU_05.check(CON_FIXTURE))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU05_ClaseQueDuerme");
    }

    @Test
    @DisplayName("ningún test del proyecto (fuera de los fixtures) llama a Thread.sleep")
    void ningunTestDuerme() {
        assertThatCode(() -> ReglasDeLaCasa.AU_05.check(TODO_MENOS_FIXTURES))
                .doesNotThrowAnyException();
    }
}
