package cl.dgt.tareas.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Quién soy: el puerto basta para distinguir dos instancias en el paso 5.
@Component
public class Instancia {

    private final String nombre;

    public Instancia(@Value("${server.port}") int puerto) {
        this.nombre = "instancia-" + puerto;
    }

    public String nombre() {
        return nombre;
    }
}
