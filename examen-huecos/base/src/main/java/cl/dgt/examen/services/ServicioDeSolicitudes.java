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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioDeSolicitudes {

    private final SolicitudRepository solicitudes;
    private final OficinaRepository oficinas;

    // =========================================================================
    //  HUECO 08 · El tope del listado, que no se escribe en el codigo
    // -------------------------------------------------------------------------
    //  El listado de `ultimas()` no puede devolver todo: se corta en un tope.
    //
    //  Ese numero NO va escrito en esta clase. Va en la configuracion, y esta
    //  clase lo recibe. Hay que ponerlo en `application.yml`, bajo `dgt.examen`,
    //  y hacerlo llegar hasta aqui.
    //
    //  El valor que espera el test es 3.
    //
    //  ESTA LISTO CUANDO · pasa el test H-08
    // =========================================================================

    public ServicioDeSolicitudes(SolicitudRepository solicitudes,
                                 OficinaRepository oficinas) {
        this.solicitudes = solicitudes;
        this.oficinas = oficinas;
    }

    @Transactional(readOnly = true)
    public long conteoPorEstado(String estado) {
        // =========================================================================
        //  HUECO 03 · Cuantas solicitudes hay en ese estado
        // -------------------------------------------------------------------------
        //  Devuelve el numero. Va con el hueco 03 del repositorio.
        //
        //  ESTA LISTO CUANDO · pasa el test H-03
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 03");
    }

    @Transactional(readOnly = true)
    public List<SolicitudBreve> recientesPorEstado(String estado) {
        // =========================================================================
        //  HUECO 04 · Las solicitudes de un estado, de la mas reciente a la mas antigua
        // -------------------------------------------------------------------------
        //  Devuelve la lista como `SolicitudBreve`. Va con el hueco 04 del repositorio.
        //
        //  ESTA LISTO CUANDO · pasa el test H-04
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 04");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalPorEstado(String estado) {
        // =========================================================================
        //  HUECO 07 · Cuanto suman las solicitudes de un estado
        // -------------------------------------------------------------------------
        //  Devuelve la SUMA de los montos de las solicitudes de ese estado.
        //  Si no hay ninguna, la suma es cero, no un fallo.
        //
        //  El repositorio ya trae resuelta la consulta que las trae.
        //
        //  ESTA LISTO CUANDO · pasa el test H-07
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 07");
    }

    @Transactional(readOnly = true)
    public List<SolicitudBreve> ultimas() {
        // =========================================================================
        //  HUECO 08 · Las ultimas solicitudes, cortadas en el tope
        // -------------------------------------------------------------------------
        //  Devuelve las solicitudes mas recientes primero, cortadas en el tope
        //  configurado. El repositorio ya trae resuelta la consulta ordenada.
        //
        //  ESTA LISTO CUANDO · pasa el test H-08
        // =========================================================================
        throw new UnsupportedOperationException("HUECO 08");
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
