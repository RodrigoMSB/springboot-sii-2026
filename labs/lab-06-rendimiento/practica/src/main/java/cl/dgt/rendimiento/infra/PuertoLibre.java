package cl.dgt.rendimiento.infra;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/** Comprueba que el puerto de la base esté libre antes de arrancarla. */
public final class PuertoLibre {

    private PuertoLibre() {
    }

    public static void exigir(int puerto) {
        if (estaLibre(puerto)) {
            return;
        }
        System.err.println(mensaje(puerto));
        System.exit(1);
    }

    private static boolean estaLibre(int puerto) {
        try (ServerSocket sonda = new ServerSocket()) {
            // Sin SO_REUSEADDR: si hay alguien escuchando, esto tiene que fallar.
            sonda.setReuseAddress(false);
            sonda.bind(new InetSocketAddress(InetAddress.getByName("localhost"), puerto), 1);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    static String mensaje(int puerto) {
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String comando = windows
                ? "     netstat -ano | findstr :" + puerto + "\n"
                + "     taskkill /F /PID <el PID de la fila que dice LISTENING>"
                : "     lsof -ti:" + puerto + " | xargs kill -9";

        return """
                =============================================================================
                 EL PUERTO %d YA ESTA OCUPADO
                -----------------------------------------------------------------------------
                 Ahi es donde este proyecto levanta su PostgreSQL, asi que no puede arrancar.

                 Lo mas probable: quedo un PostgreSQL vivo de una corrida anterior de ESTE
                 mismo proyecto. Al cerrar con Ctrl+C, o al cerrar la terminal de golpe, el
                 motor a veces sobrevive al programa que lo levanto.

                 NO es un error de tu codigo.

                 Cierra el que quedo y vuelve a arrancar:

                %s

                 Si sigue igual, es que ese mismo proyecto TODAVIA corre en otra terminal:
                 vuelve a ella y cierralo con Ctrl+C. La aplicacion retiene el archivo
                 .datos-pg/epg-lock aunque su PostgreSQL ya no este.

                 Y si tampoco es eso, hay otro programa usando el %d en tu maquina.
                =============================================================================
                """.formatted(puerto, comando, puerto);
    }
}
