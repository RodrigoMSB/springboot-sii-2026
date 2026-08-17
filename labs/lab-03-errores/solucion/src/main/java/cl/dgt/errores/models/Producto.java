package cl.dgt.errores.models;

/**
 * Un producto del catálogo. Viene dado: hoy no se toca.
 *
 * <p>Es el mismo record del Lab 02, y por la misma razón: un dato sin anotaciones, sin imports y
 * sin idea de que exista Spring. Es lo que SALE por la API; lo que entra es
 * {@link cl.dgt.errores.dto.ProductoNuevoDto}, que no trae id porque el id lo pone el servidor.
 *
 * @param id     identificador
 * @param nombre cómo se llama
 * @param precio en pesos, sin decimales
 */
public record Producto(Long id, String nombre, int precio) {
}
