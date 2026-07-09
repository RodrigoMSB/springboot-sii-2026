package cl.dgt.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** El backend de la Dirección General de Tributación. Lo que hay detrás del botón. */
@SpringBootApplication
public class DgtTramitesApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DgtTramitesApiApplication.class, args);
    }
}
