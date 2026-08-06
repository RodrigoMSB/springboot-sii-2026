package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.config.DgtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · La configuración es tipada e inmutable, y se valida al arrancar.
 *
 * <p>No se prueba "que exista un getter". Se prueba lo que un {@code @Value} no puede dar:
 * que un valor inválido <strong>impida el arranque</strong>, en vez de convertirse en un
 * folio de tres dígitos emitido en producción.
 */
class T3_DgtPropertiesTest {

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DgtProperties.class)
    static class Config {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(Config.class);

    @Test
    @DisplayName("Una configuración válida se enlaza a un record inmutable")
    void seEnlazaLaConfiguracionValida() {
        runner.withPropertyValues(
                        "dgt.institucion=Dirección General de Tributación",
                        "dgt.folio.prefijo=DGT",
                        "dgt.folio.largo=8")
                .run(contexto -> {
                    assertThat(contexto).hasNotFailed();
                    DgtProperties props = contexto.getBean(DgtProperties.class);
                    assertThat(props.institucion()).isEqualTo("Dirección General de Tributación");
                    assertThat(props.folio().prefijo()).isEqualTo("DGT");
                    assertThat(props.folio().largo()).isEqualTo(8);
                });
    }

    @Test
    @DisplayName("Un folio de 3 dígitos impide arrancar: es una regla de negocio, no un typo")
    void largoInsuficienteRompeElArranque() {
        runner.withPropertyValues(
                        "dgt.institucion=DGT",
                        "dgt.folio.prefijo=DGT",
                        "dgt.folio.largo=3")
                .run(contexto -> assertThat(contexto)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("dgt.folio.largo"));
    }

    @Test
    @DisplayName("Una institución en blanco impide arrancar")
    void institucionEnBlancoRompeElArranque() {
        runner.withPropertyValues(
                        "dgt.institucion=",
                        "dgt.folio.prefijo=DGT",
                        "dgt.folio.largo=8")
                .run(contexto -> assertThat(contexto).hasFailed());
    }
}
