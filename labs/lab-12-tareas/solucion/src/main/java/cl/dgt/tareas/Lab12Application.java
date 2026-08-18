package cl.dgt.tareas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// Las dos anotaciones que encienden lo de hoy. Sin ellas, @Scheduled y @Async no hacen nada
// y no avisan.
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class Lab12Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab12Application.class, args);
    }
}
