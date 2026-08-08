package cl.dgt.contribuyentes;

/**
 * Lo que este servicio publica por su API.
 *
 * @param rut         el RUT consultado
 * @param razonSocial el nombre del titular
 * @param atendidoPor qué instancia respondió — ver abajo, no es decoración
 *
 * <p><strong>Sobre {@code atendidoPor}:</strong> en un sistema de verdad este
 * campo no iría en el cuerpo de la respuesta (iría en una cabecera, o en la
 * traza distribuida del Lab 10). Aquí está a propósito y a la vista, porque el
 * bloque 3 del laboratorio consiste en mirar CÓMO se reparten las peticiones
 * entre dos instancias, y la forma más honesta de verlo es que cada respuesta
 * diga quién la firmó.
 *
 * <p>El día que quieras esto en producción, la respuesta es: cabecera
 * {@code X-Servido-Por} más una traza con {@code traceId}, no un campo del DTO.
 */
public record FichaContribuyente(String rut, String razonSocial, String atendidoPor) {
}
