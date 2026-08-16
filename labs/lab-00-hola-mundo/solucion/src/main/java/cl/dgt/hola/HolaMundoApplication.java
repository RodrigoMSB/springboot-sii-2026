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
// =============================================================================
//  @SpringBootApplication — la anotación que lo enciende todo
// -----------------------------------------------------------------------------
//  No es una anotación, son tres en una:
//
//   · @SpringBootConfiguration — «en esta clase puede haber definiciones de
//     objetos»: es lo que hace que Spring mire el método run() de abajo.
//   · @EnableAutoConfiguration — «mira lo que hay en el classpath y configúralo
//     solo». En este lab no hay nada que configurar; en el Lab 01, esta línea es
//     la que levantará un servidor web solo por haber añadido una dependencia.
//   · @ComponentScan — «busca clases anotadas a partir de ESTE paquete y hacia
//     abajo». Por eso la clase principal vive en cl.dgt.hola y todo lo demás
//     cuelga de ahí: si se subiera de paquete, dejaría de encontrarse.
// =============================================================================
@SpringBootApplication
public class HolaMundoApplication {

    // =========================================================================
    //  EL PUNTO DE ENTRADA
    // -------------------------------------------------------------------------
    //  Es un main de Java corriente: se puede ejecutar desde el IDE con el botón
    //  de siempre. Lo único distinto es a quién llama.
    //  SpringApplication.run recibe la clase anotada —para saber desde dónde
    //  escanear— y los argumentos de la línea de comandos, que Spring convierte
    //  en configuración: `--server.port=9000` funcionaría sin escribir código.
    //  Qué devuelve: el contenedor ya construido. Aquí se ignora porque no hace
    //  falta; en un test o en una app de escritorio se guardaría para cerrarlo.
    // =========================================================================
    public static void main(String[] args) {
        SpringApplication.run(HolaMundoApplication.class, args);
    }

    // =========================================================================
    //  LO QUE SE EJECUTA AL ARRANCAR
    // -------------------------------------------------------------------------
    //  @Bean sobre un método significa: «Spring, llama tú a este método al
    //  arrancar y quédate con lo que devuelva». El objeto devuelto pasa a vivir
    //  en el contenedor, y su nombre es el del método: `run`.
    //  Un CommandLineRunner es una interfaz de una sola función que Spring busca
    //  después de tener la aplicación lista y ejecuta una vez. Nadie la llama
    //  desde el main: por eso el mensaje sale DESPUÉS del banner y del «Started».
    //  El parámetro `args` de la lambda son los mismos argumentos del main; aquí
    //  no se usan, pero es donde llegarían.
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
