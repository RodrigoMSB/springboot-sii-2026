package cl.dgt.observabilidad.observabilidad;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

// El motor arranca ANTES que el contexto y se maneja aparte del DataSource: así el paso 5
// puede tirarlo y levantarlo mientras la aplicación sigue viva.
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
