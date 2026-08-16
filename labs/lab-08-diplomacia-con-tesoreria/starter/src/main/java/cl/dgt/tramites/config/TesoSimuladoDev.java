package cl.dgt.tramites.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * TESO simulado para el perfil {@code dev}: el doble de Tesorería, dentro de este mismo proceso.
 *
 * <p>Antes esto era un servicio del {@code compose.yaml} —la imagen {@code wiremock/wiremock}
 * escuchando en el 8089— y el alumno necesitaba Docker para verlo. Las máquinas del SII no lo
 * tienen ni pueden instalarlo, así que el impostor llega por el mismo camino que la base de
 * datos: como una dependencia Maven que corre in-process. WireMock nació librería; la imagen
 * era un envoltorio sobre ella, y quitarlo no quita nada.
 *
 * <p><strong>El puerto sigue siendo el 8089, y eso es deliberado.</strong> Las guías, los
 * {@code curl} del alumno y el {@code --teso-lento} de los scripts lo nombran; cambiarlo
 * obligaría a reescribir material que ya es correcto. La API de administración de WireMock
 * sigue viva en ese mismo puerto, así que todo lo que el alumno sabía hacerle a TESO se sigue
 * haciendo igual:
 *
 * <pre>
 *   curl http://localhost:8089/__admin/mappings
 * </pre>
 *
 * <p><strong>Atado a 127.0.0.1.</strong> Por defecto WireMock escucha en todas las interfaces, y
 * en Windows eso saca el cartel del Firewall pidiendo un permiso de administrador que el alumno
 * de una máquina corporativa no tiene (SPEC-024 · A2.3). TESO solo tiene que ser alcanzable
 * desde esta máquina: es un simulador de laboratorio, no un servicio publicado.
 *
 * <p><strong>Solo en {@code dev}.</strong> En {@code prod} esta clase no se carga: allí Tesorería
 * existe de verdad y {@code DGT_TESO_URL} apunta a la suya. Un simulador que se cuele en
 * producción confirmaría pagos que nadie hizo.
 */
@Configuration(proxyBeanMethods = false)
@Profile("dev")
@ConditionalOnProperty(name = "dgt.teso-simulado.enabled", havingValue = "true", matchIfMissing = true)
class TesoSimuladoDev {

    /** Puerto fijo: lo nombran las guías y el flag {@code --teso-lento}. */
    static final int PUERTO = 8089;

    /**
     * El servidor del impostor. {@code destroyMethod = "stop"} es lo que libera el 8089 cuando el
     * contexto de Spring se cierra: sin eso, el segundo arranque del lab encontraría el puerto
     * tomado por el primero.
     *
     * <p>Las respuestas se cargan de {@code src/main/resources/teso}, que tiene la estructura que
     * WireMock espera ({@code mappings/}). Siguen siendo los mismos JSON versionados que antes
     * montaba el compose: reproducibles y revisables en el diff.
     */
    @Bean(destroyMethod = "stop")
    WireMockServer tesoSimulado() {
        WireMockServer teso = new WireMockServer(options()
                .port(PUERTO)
                .bindAddress("127.0.0.1")
                .usingFilesUnderClasspath("teso"));
        teso.start();
        return teso;
    }
}
