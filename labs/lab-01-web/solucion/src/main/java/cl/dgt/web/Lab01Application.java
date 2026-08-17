package cl.dgt.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * La misma clase del Lab 00, sin una línea de diferencia.
 *
 * <p>Lo único que cambió está en el {@code pom.xml}: una dependencia más. Con ella la aplicación
 * ya no arranca y termina — levanta un servidor y <strong>se queda esperando</strong>. Se apaga
 * con Ctrl+C.
 */
// =============================================================================
//  POR QUE ESTA CLASE NO CAMBIA Y AUN ASI PASA ALGO DISTINTO
// -----------------------------------------------------------------------------
//  De las tres anotaciones que hay dentro de @SpringBootApplication, la que
//  trabaja hoy es @EnableAutoConfiguration: al arrancar mira qué hay en el
//  classpath y configura lo que encuentra.
//  Al aparecer `spring-boot-starter-web`, encuentra Tomcat y un servidor web, y
//  los levanta solo. Por eso la aplicación deja de terminar: ya no tiene nada
//  más que hacer, pero hay un servidor escuchando y el programa no puede morirse
//  mientras eso siga abierto.
//  Y @ComponentScan es la que encontrará el controller: basta con que esté en un
//  paquete por debajo de `cl.dgt.web`. Nadie lo registra a mano.
// =============================================================================
@SpringBootApplication
public class Lab01Application {

    // El mismo main del Lab 00: arranca el contenedor y le pasa los argumentos
    // de la línea de comandos, que Spring lee como configuración.
    public static void main(String[] args) {
        SpringApplication.run(Lab01Application.class, args);
    }
}
