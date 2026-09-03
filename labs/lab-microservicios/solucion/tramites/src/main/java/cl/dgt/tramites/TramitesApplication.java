package cl.dgt.tramites;

import cl.dgt.tramites.infra.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class TramitesApplication {

    static final int PUERTO_BASE = 55461;

    public static void main(String[] args) throws IOException {
        new MotorDePostgres(PUERTO_BASE).levantar();
        SpringApplication.run(TramitesApplication.class, args);
    }
}
