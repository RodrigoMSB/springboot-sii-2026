package cl.dgt.examen.dto;

import cl.dgt.examen.entities.Solicitud;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Viene resuelto: es el cuerpo con el que salen las solicitudes. */
public record SolicitudBreve(Long id, String tipo, String estado, LocalDate fecha, BigDecimal monto) {

    public static SolicitudBreve de(Solicitud solicitud) {
        return new SolicitudBreve(solicitud.getId(), solicitud.getTipo(), solicitud.getEstado(),
                solicitud.getFecha(), solicitud.getMonto());
    }
}
