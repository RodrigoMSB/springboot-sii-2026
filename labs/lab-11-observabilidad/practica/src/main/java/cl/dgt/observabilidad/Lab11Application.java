package cl.dgt.observabilidad;

import cl.dgt.observabilidad.observabilidad.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Lab11Application {

    static final int PUERTO_BASE = 55442;

    public static void main(String[] args) throws IOException {
        MotorDePostgres motor = new MotorDePostgres(PUERTO_BASE);
        motor.levantar();

        SpringApplication aplicacion = new SpringApplication(Lab11Application.class);
        aplicacion.addInitializers(contexto ->
                contexto.getBeanFactory().registerSingleton("motorDePostgres", motor));
        aplicacion.run(args);
    }
}
