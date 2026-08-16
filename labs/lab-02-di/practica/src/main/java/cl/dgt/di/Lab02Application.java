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
 * <p>Este archivo no se toca en todo el laboratorio. Lo que se escribe va en {@code models/},
 * {@code repositories/}, {@code services/} y {@code controllers/}, que hoy llegan vacíos.
 */
@SpringBootApplication
public class Lab02Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab02Application.class, args);
    }
}
