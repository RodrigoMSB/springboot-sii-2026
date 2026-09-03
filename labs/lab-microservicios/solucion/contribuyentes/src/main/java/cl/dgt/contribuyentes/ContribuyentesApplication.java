package cl.dgt.contribuyentes;

import cl.dgt.contribuyentes.infra.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ContribuyentesApplication {

    // La base de ESTE servicio. Tiene que cuadrar con el `spring.datasource.url`.
    static final int PUERTO_BASE = 55460;

    public static void main(String[] args) throws IOException {
        // La base arranca antes del contexto: Flyway y el pool se conectan al construirse.
        new MotorDePostgres(PUERTO_BASE).levantar();
        SpringApplication.run(ContribuyentesApplication.class, args);
    }
}
