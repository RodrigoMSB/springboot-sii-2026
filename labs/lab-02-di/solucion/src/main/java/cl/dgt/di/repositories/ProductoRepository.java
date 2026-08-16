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
 */
public interface ProductoRepository {

    /** Todos los productos. */
    List<Producto> todos();

    /** Uno, si existe. {@code Optional} porque preguntar por un id que no está es normal. */
    Optional<Producto> porId(Long id);
}
