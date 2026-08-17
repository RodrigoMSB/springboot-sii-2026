package cl.dgt.errores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * La misma clase de siempre.
 *
 * <p>Todo lo del laboratorio de hoy —el manejador de errores— se conecta solo, por anotación. Este
 * archivo no se entera de que existe: {@code @ComponentScan} encuentra el
 * {@code @RestControllerAdvice} igual que encuentra el controller, y Spring lo enchufa al
 * mecanismo que atiende las peticiones. No hay ninguna línea que los presente.
 */
@SpringBootApplication
public class Lab03Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab03Application.class, args);
    }
}
