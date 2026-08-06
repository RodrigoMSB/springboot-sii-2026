package cl.dgt.tramites.web.dto;

/** Lo que la DGT cuenta de un adjunto: su id, el nombre SANEADO, y su tipo REAL. */
public record AdjuntoDto(Long id, String nombreArchivo, String mimeReal) {
}
