package cl.dgt.hola;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HolaMundoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolaMundoApplication.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println();
            System.out.println("  Hola, mundo. Esto lo escribí yo.");
            System.out.println();
        };
    }
}
