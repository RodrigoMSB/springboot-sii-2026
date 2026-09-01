package cl.dgt.contribuyentes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContribuyentesApplication {

    // En el laboratorio, aquí había un `new MotorDePostgres(55460).levantar()`:
    // el servicio arrancaba su propia base embebida antes que el contexto, y con
    // ella las dos guardas de puerto y candado. Aquí no hay nada de eso — la base
    // es un contenedor aparte que Compose levanta antes, y `depends_on` espera a
    // que esté sana. Un `main` que no tiene que montar su infraestructura.
    public static void main(String[] args) {
        SpringApplication.run(ContribuyentesApplication.class, args);
    }
}
