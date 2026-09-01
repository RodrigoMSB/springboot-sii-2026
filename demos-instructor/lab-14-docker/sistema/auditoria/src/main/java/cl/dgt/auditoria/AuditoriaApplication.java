package cl.dgt.auditoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ver la nota de ContribuyentesApplication: la base ya no la levanta el
// servicio, la levanta el orquestador.
@SpringBootApplication
public class AuditoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditoriaApplication.class, args);
    }
}
