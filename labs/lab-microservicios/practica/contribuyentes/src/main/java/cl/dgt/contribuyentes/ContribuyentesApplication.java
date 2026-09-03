package cl.dgt.contribuyentes;

import cl.dgt.contribuyentes.infra.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ContribuyentesApplication {

    static final int PUERTO_BASE = 55450;

    public static void main(String[] args) throws IOException {
        new MotorDePostgres(PUERTO_BASE).levantar();
        SpringApplication.run(ContribuyentesApplication.class, args);
    }
}
