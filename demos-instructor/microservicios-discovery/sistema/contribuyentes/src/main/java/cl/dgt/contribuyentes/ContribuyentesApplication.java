package cl.dgt.contribuyentes;

import cl.dgt.contribuyentes.infra.MotorDePostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ContribuyentesApplication {

    // =========================================================================
    //  EL PUERTO QUE NO SE PUDO CENTRALIZAR — y conviene señalarlo
    // =========================================================================
    //  Todo lo demás de este servicio vive en el Config Server: el puerto en el
    //  que escucha, la URL de la base, JPA, Flyway, el formato del log. Éste no.
    //
    //  Y no es un descuido: es un ORDEN que no se puede invertir. La base tiene
    //  que estar arriba ANTES de `SpringApplication.run`, porque Flyway y el pool
    //  se conectan al construirse — y en ese momento el `Environment` de Spring
    //  todavía no existe, así que la configuración remota tampoco.
    //
    //  Tiene que cuadrar a mano con el `spring.datasource.url` de
    //  `config-repo/contribuyentes.yml`. Es el tipo de acoplamiento que un Config
    //  Server NO arregla, y decirlo vale más que esconderlo: la configuración
    //  centralizada llega hasta donde llega el arranque del framework.
    // =========================================================================
    static final int PUERTO_BASE = 55480;

    public static void main(String[] args) throws IOException {
        // La base arranca antes del contexto: Flyway y el pool se conectan al construirse.
        new MotorDePostgres(PUERTO_BASE).levantar();
        SpringApplication.run(ContribuyentesApplication.class, args);
    }
}
