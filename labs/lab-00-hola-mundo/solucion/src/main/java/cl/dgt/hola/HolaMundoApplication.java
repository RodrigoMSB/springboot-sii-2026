package cl.dgt.hola;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * La aplicación Spring Boot más pequeña que se puede escribir: una clase, una anotación y un
 * {@code main}.
 *
 * <p>Todo lo que venga después en el curso —endpoints, inyección, base de datos— se cuelga de
 * aquí. No hay un segundo mecanismo escondido.
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
    //  Qué se espera ver: el mensaje aparece DESPUÉS del banner, no antes.
    //  Para pensar: ¿quién llamó a este método, si en el main no aparece?
    // =========================================================================
    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println();
            System.out.println("  Hola, mundo. Esto lo escribí yo.");
            System.out.println();
        };
    }
}
