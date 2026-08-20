package cl.dgt.auditoria;

import cl.dgt.auditoria.infra.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class AuditoriaApplication {

    static final int PUERTO_BASE = 55462;

    public static void main(String[] args) throws IOException {
        new MotorDePostgres(PUERTO_BASE).levantar();
        SpringApplication.run(AuditoriaApplication.class, args);
    }
}
