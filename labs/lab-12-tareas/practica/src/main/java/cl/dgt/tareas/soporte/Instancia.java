package cl.dgt.tareas.soporte;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
