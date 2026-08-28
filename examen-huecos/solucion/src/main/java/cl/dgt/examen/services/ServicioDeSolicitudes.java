package cl.dgt.examen.services;

import cl.dgt.examen.dto.CambioDeEstado;
import cl.dgt.examen.dto.NuevaSolicitud;
import cl.dgt.examen.dto.SolicitudBreve;
import cl.dgt.examen.entities.Oficina;
import cl.dgt.examen.entities.Solicitud;
import cl.dgt.examen.exceptions.OficinaNoEncontrada;
import cl.dgt.examen.exceptions.SolicitudNoEncontrada;
import cl.dgt.examen.repositories.OficinaRepository;
import cl.dgt.examen.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioDeSolicitudes {

    private final SolicitudRepository solicitudes;
    private final OficinaRepository oficinas;
    private final int topeDeListado;

    public ServicioDeSolicitudes(SolicitudRepository solicitudes,
                                 OficinaRepository oficinas,
                                 @Value("${dgt.examen.tope-de-listado}") int topeDeListado) {
        this.solicitudes = solicitudes;
        this.oficinas = oficinas;
        this.topeDeListado = topeDeListado;
    }

    @Transactional(readOnly = true)
    public long conteoPorEstado(String estado) {
        return solicitudes.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<SolicitudBreve> recientesPorEstado(String estado) {
        return solicitudes.findByEstadoOrderByFechaDesc(estado).stream()
                .map(SolicitudBreve::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal totalPorEstado(String estado) {
        return solicitudes.findByEstado(estado).stream()
                .map(Solicitud::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<SolicitudBreve> ultimas() {
        return solicitudes.findAllByOrderByFechaDesc().stream()
                .limit(topeDeListado)
                .map(SolicitudBreve::de)
                .toList();
    }

    @Transactional
    public SolicitudBreve crear(NuevaSolicitud nueva) {
        Oficina oficina = oficinas.findByCodigo(nueva.oficinaCodigo())
                .orElseThrow(() -> new OficinaNoEncontrada(nueva.oficinaCodigo()));

        return SolicitudBreve.de(solicitudes.save(new Solicitud(
                nueva.tipo(), nueva.estado(), nueva.fecha(), nueva.monto(), oficina)));
    }

    /** Viene resuelto: es el endpoint sobre el que se prueba el 400. */
    @Transactional
    public SolicitudBreve cambiarEstado(Long id, CambioDeEstado cambio) {
        Solicitud solicitud = solicitudes.findById(id)
                .orElseThrow(() -> new SolicitudNoEncontrada(id));
        solicitud.cambiarEstadoA(cambio.estado());
        return SolicitudBreve.de(solicitud);
    }
}
