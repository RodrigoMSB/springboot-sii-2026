package cl.dgt.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TramitesApplication {

    // Ver la nota de ContribuyentesApplication: la base ya no la levanta el
    // servicio, la levanta el orquestador.
    public static void main(String[] args) {
        SpringApplication.run(TramitesApplication.class, args);
    }
}
