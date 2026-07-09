package cl.dgt.tramites.dominio;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.TransicionIlegalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * RN: BORRADOR → PRESENTADO → PAGADO → FOLIADO, sin saltos ni retrocesos.
 *
 * <p>El test recorre la MATRIZ COMPLETA de 4×4 transiciones. Probar solo el camino feliz
 * demostraría que las transiciones legales funcionan; lo que hay que demostrar es que las
 * otras doce están cerradas.
 */
class MaquinaDeEstadosTest {

    private static final Set<List<EstadoTramite>> LEGALES = Set.of(
            List.of(EstadoTramite.BORRADOR, EstadoTramite.PRESENTADO),
            List.of(EstadoTramite.PRESENTADO, EstadoTramite.PAGADO),
            List.of(EstadoTramite.PAGADO, EstadoTramite.FOLIADO));

    /** Las 16 combinaciones posibles: 3 legales, 13 ilegales. */
    static Stream<Arguments> matrizCompleta() {
        List<Arguments> casos = new ArrayList<>();
        for (EstadoTramite origen : EstadoTramite.values()) {
            for (EstadoTramite destino : EstadoTramite.values()) {
                casos.add(Arguments.of(origen, destino, LEGALES.contains(List.of(origen, destino))));
            }
        }
        return casos.stream();
    }

    @ParameterizedTest(name = "{0} -> {1} : legal={2}")
    @MethodSource("matrizCompleta")
    void laMatrizCompletaDeTransiciones(EstadoTramite origen, EstadoTramite destino, boolean legal) {
        assertThat(origen.puedeTransicionarA(destino)).isEqualTo(legal);

        Tramite tramite = tramiteEn(origen);
        if (legal) {
            assertThatNoException().isThrownBy(() -> tramite.transicionarA(destino));
            assertThat(tramite.getEstado()).isEqualTo(destino);
        } else {
            assertThatThrownBy(() -> tramite.transicionarA(destino))
                    .isInstanceOf(TransicionIlegalException.class);
            assertThat(tramite.getEstado())
                    .as("una transición ilegal no puede dejar el trámite a medio camino")
                    .isEqualTo(origen);
        }
    }

    @Test
    @DisplayName("Solo hay 3 transiciones legales entre 16 combinaciones")
    void soloTresPuertasAbiertas() {
        long legales = matrizCompleta().filter(a -> (boolean) a.get()[2]).count();
        assertThat(legales).isEqualTo(3);
        assertThat(matrizCompleta().count()).isEqualTo(16);
    }

    @Test
    @DisplayName("FOLIADO es terminal: un folio emitido no se borra")
    void foliadoEsTerminal() {
        assertThat(EstadoTramite.FOLIADO.esTerminal()).isTrue();
        assertThat(EstadoTramite.BORRADOR.esTerminal()).isFalse();
    }

    /** Lleva un trámite recién creado hasta el estado pedido, por el camino legal. */
    private static Tramite tramiteEn(EstadoTramite objetivo) {
        Tramite tramite = new Tramite(new Contribuyente("11111111-1", "Valentina Rojas", 12), "DECLARACION_F29");
        for (EstadoTramite paso : List.of(EstadoTramite.PRESENTADO, EstadoTramite.PAGADO, EstadoTramite.FOLIADO)) {
            if (tramite.getEstado() == objetivo) {
                break;
            }
            tramite.transicionarA(paso);
        }
        return tramite;
    }
}
