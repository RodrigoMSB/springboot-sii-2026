package cl.dgt.registro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * El registro del sistema. No tiene controladores, ni base de datos, ni lógica:
 * `@EnableEurekaServer` trae un servidor completo con su panel en `/`.
 *
 * <p>Lo que hace, en una frase: guarda una lista de «este servicio se llama ASÍ y
 * vive AQUÍ», la reparte a quien la pida, y tacha al que deja de dar señales de
 * vida. Una guía telefónica que se escribe sola.
 *
 * <p>Y lo que NO hace, que importa igual: no enruta nada, no balancea nada y no
 * está en el camino de ninguna petición. Cuando trámites llama a contribuyentes,
 * el tráfico va directo — el registro solo dijo la dirección, antes. Por eso
 * apagarlo no corta el sistema, y por eso el bloque 4 de la demostración se puede
 * hacer en vivo sin miedo.
 */
@SpringBootApplication
@EnableEurekaServer
public class RegistroApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistroApplication.class, args);
    }
}
