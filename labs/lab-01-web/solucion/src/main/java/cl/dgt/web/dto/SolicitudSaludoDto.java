package cl.dgt.web.dto;

// El nombre del campo del record ES la clave del JSON que se recibe.
public record SolicitudSaludoDto(String nombre, boolean formal) {
}
