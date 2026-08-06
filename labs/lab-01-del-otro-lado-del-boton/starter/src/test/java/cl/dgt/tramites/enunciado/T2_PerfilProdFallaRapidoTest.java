package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.DgtTramitesApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TODO_2 · El perfil {@code prod} falla rápido si falta una variable de entorno.
 *
 * <p>Un `${DGT_DB_PASSWORD}` sin valor por defecto no admite omisiones: la aplicación no
 * arranca, y el mensaje nombra la propiedad que falta.
 *
 * <p>Lo contrario —`${DGT_DB_PASSWORD:cambiame}`— arranca en silencio y explota un martes
 * a las tres de la mañana, lejos de quien lo escribió. Fallar rápido es una decisión de
 * diseño, no una torpeza.
 *
 * <p>Transcribe el mensaje exacto que ves aquí: te lo pide el reporte entregable.
 */
class T2_PerfilProdFallaRapidoTest {

    @Test
    @DisplayName("En prod, sin DGT_DB_URL/USER/PASSWORD, la aplicación NO arranca")
    void prodSinVariablesNoArranca() {
        assertThatThrownBy(() ->
                new SpringApplicationBuilder(DgtTramitesApiApplication.class)
                        .web(WebApplicationType.NONE)
                        .profiles("prod")
                        .run())
                .as("prod debe negarse a arrancar, y nombrar las variables que faltan")
                .hasStackTraceContaining("DGT_DB_URL")
                .hasStackTraceContaining("DGT_DB_PASSWORD")
                .hasStackTraceContaining("jamás en un archivo del repositorio");
    }
}
