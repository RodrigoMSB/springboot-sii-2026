package cl.dgt.hola;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * La aplicación Spring Boot más pequeña que se puede escribir: una clase, una anotación y un
 * {@code main}.
 *
 * <p>Este es el único archivo Java del laboratorio. Tal como llega, arranca y termina sin
 * imprimir nada tuyo: eso es el paso 1. El paso 2 llena el {@code run()}.
 */
@SpringBootApplication
public class HolaMundoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolaMundoApplication.class, args);
    }

    // =========================================================================
    //  LO QUE SE EJECUTA AL ARRANCAR
    // -------------------------------------------------------------------------
    //  Un CommandLineRunner es código que Spring corre UNA VEZ, después de tener
    //  la aplicación lista y antes de devolverte la consola. No lo llama nadie:
    //  Spring lo encuentra porque este método está anotado con @Bean, y lo
    //  ejecuta solo.
    //  Qué se espera ver: tu mensaje aparece DESPUÉS del banner, no antes.
    //  Para pensar: ¿quién llamó a este método, si en el main no aparece?
    // =========================================================================
    @Bean
    CommandLineRunner run() {
        return args -> {
            // escribe aquí
        };
    }
}
