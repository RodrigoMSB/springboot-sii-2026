package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;

import java.util.List;
import java.util.Optional;

/**
 * El contrato: qué se le puede pedir a un almacén de productos.
 *
 * <p>Una interfaz no hace nada — dice <em>qué</em> se puede pedir, no <em>cómo</em> se hace. Que
 * esto sea una interfaz y no una clase es todo el laboratorio: permite que existan dos formas
 * distintas de cumplirlo, y que el resto de la aplicación no se entere de cuál está usando.
 *
 * <p>Fíjate en que tampoco lleva anotaciones. Spring no necesita que se marque el contrato: lo que
 * marca son las clases que lo cumplen ({@code @Repository}), y luego busca por TIPO. Quien pida un
 * {@code ProductoRepository} recibirá algo que sea uno, sin decir cuál.
 */
public interface ProductoRepository {

    /**
     * Todos los productos.
     *
     * <p>Devuelve {@code List} y no un array ni un {@code Set} porque el orden importa y el tamaño
     * no se conoce de antemano. Quien implemente esto decide de dónde salen.
     */
    List<Producto> todos();

    /**
     * Uno, si existe.
     *
     * <p>{@code Optional} y no {@code Producto} a secas: preguntar por un id que no está es normal,
     * no es un error. Devolver {@code null} obligaría a quien llama a acordarse de comprobarlo —y
     * el día que se olvide, el fallo aparece lejos de aquí. {@code Optional} lo obliga a decidir.
     */
    Optional<Producto> porId(Long id);
}
