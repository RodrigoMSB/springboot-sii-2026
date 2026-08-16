package cl.dgt.errores.dto;

/**
 * Lo que entra por el POST.
 *
 * <p>Tal como llega no comprueba nada: se puede crear un producto con el nombre vacío y con precio
 * negativo, y la API lo acepta encantada. En el paso 4 se le ponen las anotaciones que lo impiden.
 *
 * @param nombre cómo se llama
 * @param precio en pesos
 */
public record ProductoNuevoDto(String nombre, int precio) {
}
