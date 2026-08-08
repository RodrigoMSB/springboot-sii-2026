package cl.dgt.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * El Config Server de la DGT: una sola fuente de verdad para la configuración.
 *
 * <p>El problema que resuelve: con cinco servicios, el timeout hacia Tesorería
 * está escrito en cinco {@code application.yml} distintos. Cambiarlo son cinco
 * ediciones, cinco compilaciones y cinco despliegues — y el día que alguien
 * cambie cuatro, nadie se va a enterar hasta que falle el quinto.
 *
 * <p>Con configuración centralizada, la verdad vive en <strong>un</strong> sitio
 * ({@code sistema/config-repo/}) y los servicios la piden al arrancar.
 *
 * <p><strong>Cómo la piden — y esto cambió, importa:</strong> hasta Spring Boot
 * 2.4 los clientes usaban un {@code bootstrap.yml} y una «fase bootstrap» previa
 * al contexto normal. Esa fase <em>ya no está en el camino por defecto</em>. Hoy
 * el cliente escribe una línea en su {@code application.yml}:
 *
 * <pre>{@code
 * spring:
 *   config:
 *     import: "configserver:http://dgt-config:8888"
 * }</pre>
 *
 * <p>Si encuentras un tutorial con {@code bootstrap.yml} y
 * {@code spring-cloud-starter-bootstrap}, estás leyendo material de hace cinco
 * años. Funciona todavía, con una dependencia extra; no es como se escribe hoy.
 *
 * <p>El backend aquí es {@code native}: archivos en disco, montados en el
 * contenedor. Ver la nota del {@code pom.xml} sobre por qué, y qué se usa en
 * producción.
 */
@SpringBootApplication
@EnableConfigServer
public class DgtConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(DgtConfigApplication.class, args);
    }
}
