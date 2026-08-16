package cl.dgt.di.repositories;

import cl.dgt.di.models.Producto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * La segunda implementación del mismo contrato, con datos que se reconocen a simple vista.
 *
 * <p>Existe para probar una idea: el resto de la aplicación —el servicio, el controller— no
 * cambia ni una línea según cuál de las dos esté puesta. Por eso los datos son ridículos: si
 * aparecen en la respuesta, se sabe al instante cuál se inyectó.
 *
 * <p>En un proyecto real esta sería la implementación para pruebas, o la que responde mientras el
 * sistema de verdad todavía no existe.
 *
 * <p>Lleva {@code @Repository} pero <strong>no</strong> {@code @Primary}: es candidata, no la
 * elegida. Su nombre de bean es {@code productoRepositoryFalso}, y es el que se escribe entre
 * comillas en el {@code @Qualifier} del paso 5.
 *
 * <p>Ojo a lo que NO tiene: ni un import distinto, ni un método de más. Cumple el mismo contrato
 * con el mismo aspecto — por eso el resto de la aplicación no puede notar la diferencia salvo
 * mirando los datos.
 */
@Repository
public class ProductoRepositoryFalso implements ProductoRepository {

    // Datos deliberadamente ridículos: si aparecen en la respuesta, se sabe al
    // instante cuál de las dos implementaciones se inyectó. Es lo que convierte
    // el paso 5 en una medición y no en una promesa.
    private static final List<Producto> DATOS = List.of(
            new Producto(1L, "PRODUCTO DE PRUEBA UNO", 1),
            new Producto(2L, "PRODUCTO DE PRUEBA DOS", 2));

    /** Mismo contrato, misma firma, otros datos. */
    @Override
    public List<Producto> todos() {
        return DATOS;
    }

    /** Igual que en la otra implementación: `equals` y no `==`, que Long es un objeto. */
    @Override
    public Optional<Producto> porId(Long id) {
        return DATOS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
