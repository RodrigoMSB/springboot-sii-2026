package cl.dgt.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * La misma clase del Lab 00, sin una línea de diferencia.
 *
 * <p>Lo único que cambió está en el {@code pom.xml}: una dependencia más. Con ella la aplicación
 * ya no arranca y termina — levanta un servidor y <strong>se queda esperando</strong>. Se apaga
 * con Ctrl+C.
 *
 * <p>Este archivo no se toca en todo el laboratorio. Lo que se escribe va en {@code controllers/}
 * y en {@code dto/}, que hoy llegan vacíos.
 */
@SpringBootApplication
public class Lab01Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab01Application.class, args);
    }
}
