package cl.dgt.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Lab01Application {

    // El mismo main del Lab 00: arranca el contenedor y le pasa los argumentos
    // de la línea de comandos, que Spring lee como configuración.
    public static void main(String[] args) {
        SpringApplication.run(Lab01Application.class, args);
    }
}
