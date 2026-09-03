package cl.dgt.tramites.infra;

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
        PuertoLibre.exigir(puerto);
        CandadoLibre.exigir(directorio);
        motor = EmbeddedPostgres.builder()
                .setPort(puerto)
                .setDataDirectory(directorio)
                .setCleanDataDirectory(false)
                .start();
        log.info("[BASE] arriba en el puerto {}", puerto);
    }
}
