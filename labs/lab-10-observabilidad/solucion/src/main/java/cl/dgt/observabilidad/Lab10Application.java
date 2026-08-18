package cl.dgt.observabilidad;

import cl.dgt.observabilidad.observabilidad.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Lab10Application {

    // `practica` usa el 55442 y `solucion` el 55443. Tiene que cuadrar con el
    // `spring.datasource.url` del application.yml.
    static final int PUERTO_BASE = 55443;

    public static void main(String[] args) throws IOException {
        // La base arranca ANTES del contexto: Flyway y el pool se conectan al construirse,
        // y con la base como un bean más llegaban antes que ella.
        MotorDePostgres motor = new MotorDePostgres(PUERTO_BASE);
        motor.levantar();

        SpringApplication aplicacion = new SpringApplication(Lab10Application.class);
        aplicacion.addInitializers(contexto ->
                contexto.getBeanFactory().registerSingleton("motorDePostgres", motor));
        aplicacion.run(args);
    }
}
