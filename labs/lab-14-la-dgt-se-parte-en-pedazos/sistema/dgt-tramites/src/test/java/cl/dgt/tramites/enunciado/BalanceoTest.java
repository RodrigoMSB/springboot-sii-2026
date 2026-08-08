package cl.dgt.tramites.enunciado;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.SimpleObjectProvider;
import reactor.core.publisher.Flux;

/**
 * Que el balanceo REPARTA, y no que simplemente exista.
 *
 * <p>Un balanceador que manda siempre a la misma instancia es indistinguible de
 * uno que funciona… hasta el día que esa instancia se cae con el triple de carga
 * que las demás. Aquí se comprueba lo único que importa: que con dos instancias
 * vivas, el tráfico se parte en dos.
 *
 * <p>Se usa el {@link RoundRobinLoadBalancer} REAL de Spring Cloud, alimentado
 * con una lista fija de dos instancias. Sin registro, sin red, sin Docker: la
 * política de reparto es una decisión de código y se prueba como tal.
 *
 * <h2>Por qué esto es determinista aunque el round-robin empiece donde quiera</h2>
 *
 * <p>{@code RoundRobinLoadBalancer} arranca en una posición <em>aleatoria</em>
 * —a propósito: si todas las instancias de tu servicio arrancaran en la posición
 * cero, todas mandarían la primera petición al mismo sitio—. Así que no se puede
 * afirmar quién atiende la primera llamada.
 *
 * <p>Lo que sí es determinista es el REPARTO: con un número par de llamadas y dos
 * instancias, salgan en el orden que salgan, tienen que tocar a la mitad cada
 * una. Eso es lo que se afirma, y es lo que de verdad significa «balancear».
 */
@DisplayName("El balanceador reparte entre las instancias vivas")
class BalanceoTest {

    private static final String SERVICIO = "dgt-contribuyentes";
    private static final int LLAMADAS = 20;

    @Test
    @DisplayName("con dos instancias, veinte llamadas se parten diez y diez")
    void repartePorMitades() {
        RoundRobinLoadBalancer balanceador = balanceadorCon(
                instancia("contribuyentes-1", "10.0.0.1"),
                instancia("contribuyentes-2", "10.0.0.2"));

        Map<String, Integer> conteo = repartir(balanceador, LLAMADAS);

        assertThat(conteo)
                .as("Con dos instancias vivas tienen que salir elegidas las dos")
                .hasSize(2);
        assertThat(conteo.values())
                .as("""
                    El reparto salió %s. Con round-robin y %d llamadas sobre 2 instancias,
                    a cada una le tocan exactamente %d. Si una se lleva más, no se está
                    balanceando: se está prefiriendo.
                    """, conteo, LLAMADAS, LLAMADAS / 2)
                .containsOnly(LLAMADAS / 2);
    }

    @Test
    @DisplayName("con una sola instancia viva, todo va a esa — sin quejarse")
    void conUnaSolaNoSeRompe() {
        RoundRobinLoadBalancer balanceador = balanceadorCon(instancia("contribuyentes-1", "10.0.0.1"));

        Map<String, Integer> conteo = repartir(balanceador, LLAMADAS);

        assertThat(conteo)
                .as("""
                    Que muera una instancia no puede romper al balanceador: tiene que
                    seguir sirviendo con lo que queda. Es la mitad del argumento a favor
                    de tener más de una.
                    """)
                .containsExactly(Map.entry("contribuyentes-1", LLAMADAS));
    }

    // -------------------------------------------------------------------------

    private Map<String, Integer> repartir(RoundRobinLoadBalancer balanceador, int llamadas) {
        Map<String, Integer> conteo = new HashMap<>();
        for (int i = 0; i < llamadas; i++) {
            Response<ServiceInstance> elegida = balanceador.choose().block();
            assertThat(elegida).isNotNull();
            assertThat(elegida.hasServer())
                    .as("El balanceador no eligió a nadie teniendo instancias vivas")
                    .isTrue();
            conteo.merge(elegida.getServer().getInstanceId(), 1, Integer::sum);
        }
        return conteo;
    }

    private RoundRobinLoadBalancer balanceadorCon(ServiceInstance... instancias) {
        List<ServiceInstance> lista = List.of(instancias);
        ServiceInstanceListSupplier proveedor = new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return SERVICIO;
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.just(lista);
            }
        };
        return new RoundRobinLoadBalancer(new SimpleObjectProvider<>(proveedor), SERVICIO);
    }

    private ServiceInstance instancia(String id, String host) {
        // Puerto cualquiera: nadie va a conectarse. Lo que se prueba es a quién
        // ELIGE el balanceador, no si el otro extremo contesta.
        return new DefaultServiceInstance(id, SERVICIO, host, 8080, false);
    }
}
