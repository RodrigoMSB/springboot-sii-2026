package cl.dgt.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * El Config Server. Como el registro, es una anotación y nada más.
 *
 * <p>Sirve por HTTP el contenido de `config-repo/`. Un servicio que arranca le
 * pregunta «¿qué configuración me toca a mí?» y recibe un JSON con sus
 * propiedades; Spring las mete en el `Environment` ANTES de que se cree un solo
 * bean, y por eso desde ahí dentro son indistinguibles de las que habrían venido
 * de un `application.yml` local.
 *
 * <p>Se puede mirar a mano, y en la demostración se mira:
 * <pre>
 *   curl http://localhost:8888/tramites/default
 * </pre>
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }
}
