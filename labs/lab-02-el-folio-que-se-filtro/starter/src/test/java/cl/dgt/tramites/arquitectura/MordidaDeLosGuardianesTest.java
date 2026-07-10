package cl.dgt.tramites.arquitectura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los meta-tests: cada guardián trae su mordida certificada.
 *
 * <p><em>Un guardián sin prueba de que muerde es un adorno.</em> {@code ArquitecturaTest}
 * demuestra que las reglas <strong>pasan</strong> sobre el código limpio. Eso, por sí
 * solo, no prueba nada: una regla mal escrita también pasa. Aquí cada regla se enfrenta a
 * una clase que la viola a propósito, y se exige que <strong>falle</strong>.
 *
 * <p>Se usan las mismas constantes de {@link ReglasDeLaCasa} que juzgan la producción: no
 * hay dos verdades.
 */
class MordidaDeLosGuardianesTest {

    private static final String FIXTURES = "cl.dgt.tramites.arquitectura.fixtures.violaciones.";
    private static final String[] MAIN = {
            "cl.dgt.tramites.domain", "cl.dgt.tramites.web",
            "cl.dgt.tramites.application", "cl.dgt.tramites.infrastructure"
    };

    /** Importa el código de producción más UN paquete de fixtures. */
    private static JavaClasses conFixture(String subpaquete) {
        String[] paquetes = new String[MAIN.length + 1];
        System.arraycopy(MAIN, 0, paquetes, 0, MAIN.length);
        paquetes[MAIN.length] = FIXTURES + subpaquete;
        return new ClassFileImporter().importPackages(paquetes);
    }

    /** La regla debe fallar, y el mensaje debe nombrar a la clase culpable. */
    private static void debeMorder(ArchRule regla, String subpaquete, String culpable) {
        assertThatThrownBy(() -> regla.check(conFixture(subpaquete)))
                .as("la regla debe cazar a %s", culpable)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(culpable);
    }



    @Test
    @DisplayName("AU-03 muerde al dominio que conoce Spring")
    void au03Muerde() {
        debeMorder(ReglasDeLaCasa.AU_03, "domain", "AU03_DominioQueConoceSpring");
    }

    @Test
    @DisplayName("AU-04 muerde al @ManyToOne sin LAZY explícito")
    void au04Muerde() {
        debeMorder(ReglasDeLaCasa.AU_04, "au04", "AU04_RelacionConFetchEager");
    }

    @Test
    @DisplayName("AU-05 muerde a quien llama Thread.sleep")
    void au05Muerde() {
        debeMorder(ReglasDeLaCasa.AU_05, "au05", "AU05_ClaseQueDuerme");
    }

    @Test
    @DisplayName("AU-06 muerde al bean inyectado por campo")
    void au06Muerde() {
        debeMorder(ReglasDeLaCasa.AU_06, "au06", "AU06_BeanInyectadoPorCampo");
    }

    @Test
    @DisplayName("AU-07 muerde a la infraestructura que conoce la web")
    void au07Muerde() {
        debeMorder(ReglasDeLaCasa.AU_07, "infrastructure", "AU07_InfraQueConoceLaWeb");
    }
}
