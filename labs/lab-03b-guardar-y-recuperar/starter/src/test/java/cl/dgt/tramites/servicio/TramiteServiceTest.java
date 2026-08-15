package cl.dgt.tramites.servicio;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * TODO_4 · Los tests que escribiste tú. Unitarios, con Mockito.
 *
 * <p>Este test vive FUERA de {@code enunciado/}: es territorio libre, tuyo. El manifiesto no
 * lo toca. El {@code 90} solo comprueba que existe y pasa.
 *
 * <p>Lo que se mockea: el repositorio (una frontera, lenta, con estado). Lo que NO se
 * mockea: el {@code TramiteService} bajo prueba (es lo que estás probando) ni un DTO (es un
 * dato, no una colaboración — mockear un {@code record} es un olor). {@code @InjectMocks}
 * construye el servicio con los mocks por constructor: por eso la inyección por constructor
 * hace los tests fáciles.
 */
@ExtendWith(MockitoExtension.class)
class TramiteServiceTest {

    @Mock TramiteRepository tramites;
    @Mock ContribuyenteRepository contribuyentes;
    @InjectMocks TramiteService servicio;

    @Test
    @DisplayName("crear persiste un trámite en BORRADOR para el contribuyente hallado")
    void crearPersisteEnBorrador() {
        Contribuyente valentina = new Contribuyente("11111111-1", "Valentina Rojas", 0);
        given(contribuyentes.findByRut("11111111-1")).willReturn(Optional.of(valentina));
        given(tramites.save(any(Tramite.class))).willAnswer(inv -> inv.getArgument(0));

        servicio.crear("11111111-1", "DECLARACION_F29");

        // ArgumentCaptor: verifica QUÉ se persistió, no solo que se llamó a save.
        ArgumentCaptor<Tramite> capturado = ArgumentCaptor.forClass(Tramite.class);
        verify(tramites).save(capturado.capture());
        assertThat(capturado.getValue().getEstado().name()).isEqualTo("BORRADOR");
        assertThat(capturado.getValue().getTipo()).isEqualTo("DECLARACION_F29");
    }

    @Test
    @DisplayName("crear para un RUT inexistente falla y NO persiste nada")
    void crearConRutInexistenteNoPersiste() {
        given(contribuyentes.findByRut("99999999-9")).willReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.crear("99999999-9", "DECLARACION_F29"))
                .isInstanceOf(ContribuyenteNoEncontradoException.class);

        verify(tramites, never()).save(any());
    }
}
