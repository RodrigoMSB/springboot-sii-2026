package cl.dgt.di.models;

/**
 * Un producto del catálogo. Es solo un dato: no sabe guardarse ni buscarse a sí mismo.
 *
 * @param id     identificador
 * @param nombre cómo se llama
 * @param precio en pesos, sin decimales
 */
public record Producto(Long id, String nombre, int precio) {
}
