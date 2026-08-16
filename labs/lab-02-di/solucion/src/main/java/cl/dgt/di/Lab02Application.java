package cl.dgt.di;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * La misma clase de siempre, sin cambios.
 *
 * <p>Vale la pena mirar lo que <strong>no</strong> tiene: ni un {@code new}, ni una lista de
 * clases que registrar, ni un archivo de configuración que diga qué se conecta con qué. Todo eso
 * lo deduce Spring al arrancar, leyendo anotaciones.
 *
 * <p>La cadena completa —controller → service → repository— la arma
 * {@code @ComponentScan}, que es una de las tres anotaciones dentro de
 * {@code @SpringBootApplication}: recorre este paquete y los de abajo, encuentra las clases
 * marcadas ({@code @RestController}, {@code @Service}, {@code @Repository}), y las construye en el
 * orden que haga falta para que cada una reciba lo que pide en su constructor.
 */
@SpringBootApplication
public class Lab02Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab02Application.class, args);
    }
}
