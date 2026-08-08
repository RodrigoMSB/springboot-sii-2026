package cl.dgt.registro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * La guía telefónica de la DGT.
 *
 * <p>Un servicio arranca, llama por teléfono al registro y dice: «soy
 * DGT-CONTRIBUYENTES, estoy en la IP tal, puerto cual, y estoy sano». Cada 30
 * segundos vuelve a llamar para decir que sigue vivo. Cuando deja de llamar, el
 * registro lo tacha de la lista.
 *
 * <p>Quien quiera hablar con DGT-CONTRIBUYENTES no pregunta por una IP: pregunta
 * por el <em>nombre</em>, y el registro le devuelve la lista de instancias vivas.
 * Ese es todo el patrón <em>service discovery</em>, y por eso los servicios de
 * este laboratorio arrancan en <strong>puerto efímero</strong>: nadie necesita
 * saber en qué puerto quedaron.
 *
 * <p>Fíjate en lo que NO hay aquí: ni un controlador, ni un servicio, ni una
 * entidad. Toda la pieza es una anotación y un archivo de configuración. El
 * panel que verás en <a href="http://localhost:8761">localhost:8761</a> viene
 * dentro del starter.
 *
 * <p><strong>Y fíjate en lo que sí hay:</strong> este registro se anota a sí
 * mismo en su propia guía ({@code register-with-eureka: true} en su
 * {@code application.yml}). Por eso el panel muestra <em>seis</em> piezas y no
 * cinco. No es obligatorio en un Eureka de un solo nodo — se hizo a propósito,
 * para que el alumno vea el sistema completo en una sola pantalla.
 */
@SpringBootApplication
@EnableEurekaServer
public class DgtRegistroApplication {

    public static void main(String[] args) {
        SpringApplication.run(DgtRegistroApplication.class, args);
    }
}
