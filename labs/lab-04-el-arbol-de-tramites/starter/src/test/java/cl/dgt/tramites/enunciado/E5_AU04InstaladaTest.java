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
 * TODO_1 · El guardián AU-04 que instalaste MUERDE.
 *
 * <p>AU-04: toda relación {@code @ManyToOne}/{@code @OneToOne} declara {@code fetch}
 * explícito. Sin él, JPA aplica un default distinto por anotación —EAGER para
 * {@code @ManyToOne}, LAZY para {@code @OneToMany}— y nadie sabe qué está pasando sin mirar
 * la especificación. No declarar no es neutralidad: es delegar a una tabla que casi nadie
 * recuerda.
 *
 * <p>Dos mitades: AU-04 pasa sobre tu código de producción (si dejaste una relación en
 * EAGER, esto te caza a ti) y muerde a su fixture.
 */
class E5_AU04InstaladaTest {

    private static final JavaClasses PRODUCCION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("cl.dgt.tramites");

    private static final JavaClasses CON_FIXTURE = new ClassFileImporter()
            .importPackages("cl.dgt.tramites.arquitectura.fixtures.violaciones.au04");

    @Test
    @DisplayName("AU-04 pasa: todas las relaciones de producción declaran fetch explícito")
    void au04PasaSobreProduccion() {
        assertThatCode(() -> ReglasDeLaCasa.AU_04.check(PRODUCCION))
                .as("si una relación quedó en EAGER (o sin declarar), esto falla")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("AU-04 muerde: caza a la relación sin fetch explícito")
    void au04MuerdeSuFixture() {
        assertThatThrownBy(() -> ReglasDeLaCasa.AU_04.check(CON_FIXTURE))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("AU04_RelacionConFetchEager");
    }
}
