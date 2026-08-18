package cl.dgt.observabilidad.observabilidad;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MotorDePostgres {

    private static final Logger log = LoggerFactory.getLogger(MotorDePostgres.class);

    private final int puerto;
    private final File directorio = new File(".datos-pg");
    private EmbeddedPostgres motor;

    public MotorDePostgres(int puerto) {
        this.puerto = puerto;
    }

    public void levantar() throws IOException {
        if (motor != null) {
            return;
        }

        // Después de la guarda de arriba, y no antes: si el motor ya está en pie, el puerto lo
        // ocupa él mismo y la comprobación mataría la aplicación al pedir /simulador/base-sana
        // dos veces seguidas.
        PuertoLibre.exigir(puerto);
        CandadoLibre.exigir(directorio);
        motor = EmbeddedPostgres.builder()
                .setPort(puerto)
                .setDataDirectory(directorio)
                .setCleanDataDirectory(false)
                .start();
        log.info("[BASE] arriba en el puerto {}", puerto);
    }

    public void caer() throws IOException {
        if (motor == null) {
            return;
        }
        motor.close();
        motor = null;
        log.warn("[BASE] APAGADA a propósito");
    }

    public boolean estaArriba() {
        return motor != null;
    }
}
