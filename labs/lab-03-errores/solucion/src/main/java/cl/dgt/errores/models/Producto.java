package cl.dgt.errores.models;

/**
 * Un producto del catálogo. Viene dado: hoy no se toca.
 *
 * @param id     identificador
 * @param nombre cómo se llama
 * @param precio en pesos, sin decimales
 */
public record Producto(Long id, String nombre, int precio) {
}
