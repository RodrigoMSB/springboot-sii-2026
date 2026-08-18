package cl.dgt.observabilidad.observabilidad;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/** Comprueba que nadie más tenga tomado el directorio de datos de la base. */
public final class CandadoLibre {

    private CandadoLibre() {
    }

    public static void exigir(File directorioDatos) {
        File candado = new File(directorioDatos, "epg-lock");
        if (estaLibre(candado)) {
            return;
        }
        System.err.println(mensaje(candado));
        System.exit(1);
    }

    private static boolean estaLibre(File candado) {
        if (!candado.isFile()) {
            // Primera corrida: todavía no hay directorio de datos, no hay con quién chocar.
            return true;
        }
        try (RandomAccessFile archivo = new RandomAccessFile(candado, "rw");
                FileChannel canal = archivo.getChannel()) {
            FileLock sonda = canal.tryLock();
            if (sonda == null) {
                return false;                       // lo tiene otro proceso
            }
            sonda.release();                        // se suelta enseguida: quien lo necesita es Zonky
            return true;
        } catch (OverlappingFileLockException e) {
            return false;                           // lo tiene esta misma JVM
        } catch (IOException e) {
            // No se pudo comprobar. No se bloquea el arranque por una duda.
            return true;
        }
    }

    static String mensaje(File candado) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String comando = windows
                ? "     taskkill /F /IM java.exe\n"
                + "     (OJO: eso cierra TODOS los labs que tengas abiertos)"
                : "     lsof -t " + candado.getPath() + " | xargs kill -9";

        return """
                =============================================================================
                 ESTE MISMO PROYECTO YA ESTA CORRIENDO
                -----------------------------------------------------------------------------
                 El archivo %s esta tomado por otra aplicacion viva, asi
                 que este arranque no puede usar la base.

                 Ese candado lo retiene el PROGRAMA, no PostgreSQL: sigue puesto aunque su
                 motor ya no este. Es lo que pasa si mataste el PostgreSQL a mano pero la
                 aplicacion que lo levanto sigue en pie.

                 NO es un error de tu codigo.

                 Ve a la terminal donde lo tienes arrancado y cierralo con Ctrl+C.

                 Si no das con esa terminal:

                %s
                =============================================================================
                """.formatted(candado.getPath(), comando);
    }
}
