package cl.dgt.tramites.application;

import cl.dgt.tramites.application.mapper.TramiteMapper;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirma el pago de un trámite consultando a Tesorería, y solo entonces lo mueve a PAGADO.
 *
 * <p>Si TESO no responde a tiempo, {@code confirmarPago} lanza {@code TesoreriaNoDisponibleException}
 * ANTES de tocar el trámite: la transacción no escribe nada, el trámite queda íntegro en PRESENTADO,
 * y la web devuelve un 503 honesto. Degradación elegante: la mala noticia rápida es buen servicio.
 */
@Service
public class PagoService {

    private final TramiteRepository tramites;
    private final TesoreriaPort tesoreria;

    public PagoService(TramiteRepository tramites, TesoreriaPort tesoreria) {
        this.tramites = tramites;
        this.tesoreria = tesoreria;
    }

    @Transactional
    public TramiteDto confirmarPago(Long tramiteId) {
        Tramite tramite = tramites.findById(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        // Si TESO cuelga, esto falla rápido (timeout) y sale por la excepción: el trámite no se toca.
        ConfirmacionPago confirmacion = tesoreria.confirmarPago(String.valueOf(tramiteId));

        if (confirmacion.confirmado()) {
            tramite.transicionarA(EstadoTramite.PAGADO);   // PRESENTADO -> PAGADO
        }
        return TramiteMapper.aDto(tramites.save(tramite));
    }
}
