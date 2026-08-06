package cl.dgt.tramites.application;

/**
 * Un trámite, como se ve en un LISTADO: solo lo que la tabla pinta. No viajan columnas que
 * nadie va a mostrar, y no viaja el árbol de relaciones — es una proyección, no la entidad.
 */
public record TramiteResumenDto(Long id, String tipo, String estado, String rutContribuyente) {
}
