package cl.dgt.tramites.servicio;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TODO_4 · Escribe TÚ los tests del servicio de trámites (≈15 min).
 *
 * <p>Este archivo es TUYO: vive fuera de {@code enunciado/}, el manifiesto no lo toca, y el
 * {@code 90} solo comprueba que existe y pasa. Bórralo y reescríbelo, o complétalo.
 *
 * <p>Qué probar, como mínimo:
 * <ol>
 *   <li>{@code crear(...)} persiste un trámite en BORRADOR — usa un {@code ArgumentCaptor}
 *       para verificar QUÉ se guardó, no solo que se llamó a {@code save}.</li>
 *   <li>{@code crear(...)} con un RUT inexistente lanza la excepción y NO persiste nada
 *       ({@code verify(repo, never()).save(...)}).</li>
 * </ol>
 *
 * <p><strong>Y una lección sobre qué NO se mockea.</strong> Abajo hay un olor plantado:
 */
@ExtendWith(MockitoExtension.class)
class TramiteServiceTest {

    @Mock TramiteRepository tramites;
    @Mock ContribuyenteRepository contribuyentes;

    // ❌ OLOR — ELIMINA ESTA LÍNEA. Un TramiteDto es un DATO (un record), no una
    // colaboración: no tiene lógica que mockear. Mockear un dato es señal de que no
    // entendiste qué es un mock. Se mockean las FRONTERAS (el repositorio), no los valores.
    @Mock TramiteDto dtoMockeadoQueNoDeberiaExistir;

    @InjectMocks TramiteService servicio;

    @Test
    void escribeTusTests() {
        // Borra este cuerpo y escribe los tuyos. Mientras esto lance, el TODO_4 está incompleto.
        throw new UnsupportedOperationException("{{TODO_4}}");
    }
}
