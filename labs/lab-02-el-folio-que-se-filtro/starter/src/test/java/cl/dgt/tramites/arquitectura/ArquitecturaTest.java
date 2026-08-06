package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las siete reglas de la casa, aplicadas al código de producción.
 *
 * <p>{@code DO_NOT_INCLUDE_TESTS}: los fixtures negativos viven en el classpath de test y
 * violan las reglas a propósito. Si contaminaran esta importación, la vigilancia del
 * {@code main} sería imposible.
 */
class ArquitecturaTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    @Test
    @DisplayName("El importador ve código de producción y ningún fixture")
    void elImportadorEstaBienApuntado() {
        assertThat(PRODUCCION).isNotEmpty();
        assertThat(PRODUCCION.stream().map(c -> c.getName()))
                .as("ningún fixture de violaciones debe colarse en la vigilancia del main")
                .noneMatch(nombre -> nombre.contains("fixtures.violaciones"));
    }



    @Test
    @DisplayName("AU-03 · el dominio no conoce la web ni Spring")
    void au03() {
        ReglasDeLaCasa.AU_03.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-04 · todo @ManyToOne/@OneToOne declara LAZY explícito")
    void au04() {
        ReglasDeLaCasa.AU_04.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-05 · nadie llama a Thread.sleep")
    void au05() {
        ReglasDeLaCasa.AU_05.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-06 · ningún bean se inyecta por campo")
    void au06() {
        ReglasDeLaCasa.AU_06.check(PRODUCCION);
    }

    @Test
    @DisplayName("AU-07 · la infraestructura no conoce la web")
    void au07() {
        ReglasDeLaCasa.AU_07.check(PRODUCCION);
    }
}
