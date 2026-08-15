package cl.dgt.tramites.application;

import java.time.LocalDateTime;

/**
 * Lo que se muestra de una observación interna, y nada más.
 *
 * <p>Vive en {@code application} y no en {@code web} por una razón que los guardianes hacen
 * cumplir: la lee tanto la capa web como la infraestructura, y AU-07 prohíbe que la
 * infraestructura conozca la web. Un tipo compartido tiene que estar por debajo de las dos.
 *
 * <p>Es un {@code record} y no la entidad: la entidad no sale por la API (AU-01 y AU-02). Aquí
 * viaja el RUT del contribuyente, no su puntaje de riesgo.
 */
public record ObservacionInternaVista(
        String rutContribuyente,
        String texto,
        String autor,
        LocalDateTime creadaEn) {
}
