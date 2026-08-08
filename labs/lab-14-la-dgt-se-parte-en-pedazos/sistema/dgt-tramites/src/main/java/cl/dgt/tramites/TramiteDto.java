package cl.dgt.tramites;

/**
 * Lo que el portal le devuelve al mundo.
 *
 * @param id                  identificador del trámite
 * @param tipo                tipo de trámite
 * @param estado              estado del trámite
 * @param rutContribuyente    el RUT — este SIEMPRE está: es dato local
 * @param nombreContribuyente el nombre — este viene del OTRO servicio, y puede
 *                            faltar
 * @param atendidoPor         qué instancia de {@code dgt-contribuyentes}
 *                            respondió, o {@code null} si no respondió ninguna
 *
 * <p>La diferencia entre los dos últimos campos y los tres primeros es la
 * arquitectura entera de este laboratorio, en un solo record: los datos locales
 * no fallan; los datos que hay que ir a buscar por la red, sí.
 */
public record TramiteDto(Long id,
                         String tipo,
                         String estado,
                         String rutContribuyente,
                         String nombreContribuyente,
                         String atendidoPor) {
}
