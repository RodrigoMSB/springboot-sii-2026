package cl.dgt.di.models;

/**
 * Un producto del catálogo. Es solo un dato: no sabe guardarse ni buscarse a sí mismo.
 *
 * <p>Esa frase es una decisión de diseño, no una casualidad. El objeto que lleva los datos no
 * conoce la base, ni el repositorio, ni la web; quien sabe buscarlos es otro. Por eso este archivo
 * no tiene ni una anotación, ni un import, ni una dependencia: se puede leer entero sin saber que
 * existe Spring.
 *
 * <p>Es un {@code record}, así que llega inmutable y con el constructor, los métodos de lectura y
 * el {@code toString()} ya hechos. Al devolverlo desde un {@code @RestController}, Jackson lo
 * convierte en JSON usando estos mismos nombres de campo.
 *
 * @param id     identificador
 * @param nombre cómo se llama
 * @param precio en pesos, sin decimales — un {@code int} basta y evita los líos de coma flotante
 */
public record Producto(Long id, String nombre, int precio) {
}
