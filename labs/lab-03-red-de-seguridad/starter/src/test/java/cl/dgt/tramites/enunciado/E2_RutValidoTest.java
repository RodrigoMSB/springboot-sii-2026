package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.web.validacion.RutValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El validador de RUT chileno (módulo 11). El caso feliz suelto engaña; el conjunto no.
 *
 * <p>Esta es la lección de la triangulación: un test parametrizado con seis RUTs no se pasa
 * hardcodeando un {@code return true}. La red atrapa lo que un hilo deja pasar.
 */
class E2_RutValidoTest {

    private final RutValidator validador = new RutValidator();

    @ParameterizedTest(name = "{0} es un RUT válido")
    @ValueSource(strings = {"11111111-1", "12345678-5", "1-9", "7654321-6"})
    @DisplayName("un RUT con dígito verificador correcto se acepta")
    void rutValido(String rut) {
        assertThat(validador.isValid(rut, null)).isTrue();
    }

    @ParameterizedTest(name = "{0} tiene dígito verificador falso")
    @ValueSource(strings = {"11111111-2", "12345678-9", "7654321-0"})
    @DisplayName("un RUT con dígito verificador falso se rechaza")
    void rutConDvFalso(String rut) {
        assertThat(validador.isValid(rut, null)).isFalse();
    }

    @ParameterizedTest(name = "\"{0}\" es basura de formato")
    @ValueSource(strings = {"no-es-un-rut", "1234", "ABCDEFG-1"})
    @NullAndEmptySource
    @DisplayName("un RUT con formato basura se rechaza sin explotar")
    void rutBasura(String rut) {
        assertThat(validador.isValid(rut, null)).isFalse();
    }
}
