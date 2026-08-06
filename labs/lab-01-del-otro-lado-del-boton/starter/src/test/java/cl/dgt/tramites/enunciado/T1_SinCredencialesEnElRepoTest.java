package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · Ningún archivo versionado contiene la credencial de producción.
 *
 * <p>Esto es un TEST, no un `grep` del validador. La diferencia importa: un `grep` en bash
 * confunde la forma del texto con la propiedad que busca, se rompe con comillas distintas
 * y no puede correr en el CI del alumno. El anti-patrón A-01 del proyecto.
 *
 * <p>Aquí no se busca "una contraseña" (imposible), sino <em>exactamente</em> la de
 * utilería que el practicante dejó en el historial. Si vuelve a aparecer en un archivo
 * versionado, esto se pone rojo antes de que llegue a un servidor.
 *
 * <p>Nota: el {@code compose.yaml} sí lleva credencial, y está bien. Es una clave de
 * laboratorio para una base desechable. La diferencia no es el archivo: es qué protege el
 * secreto y qué pasa si se filtra.
 */
class T1_SinCredencialesEnElRepoTest {

    /** Partida en trozos: así este archivo de test no es, él mismo, una filtración. */
    private static final String CLAVE_DE_UTILERIA = "Dgt" + "2026" + "Pr0d!";
    private static final String HOST_DE_PRODUCCION = "prod-db.dgt.gob.cl";

    @Test
    @DisplayName("Ningún recurso versionado contiene la credencial de producción")
    void ningunRecursoLlevaLaCredencial() throws IOException {
        Path recursos = Path.of("src", "main", "resources");
        assertThat(recursos).as("¿se corre el test desde la raíz del proyecto?").isDirectory();

        List<String> culpables = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(recursos)) {
            archivos.filter(Files::isRegularFile).forEach(archivo -> {
                try {
                    String contenido = Files.readString(archivo);
                    if (contenido.contains(CLAVE_DE_UTILERIA) || contenido.contains(HOST_DE_PRODUCCION)) {
                        culpables.add(archivo.toString());
                    }
                } catch (IOException e) {
                    // Un binario (una imagen, un .p12). No nos interesa.
                }
            });
        }

        assertThat(culpables)
                .as("Borrar la credencial de un archivo no la quita del historial de git, "
                    + "pero volver a escribirla sí la pone otra vez en producción.")
                .isEmpty();
    }

    /**
     * TODO_1 · La conexión de producción se externaliza: placeholders, no literales.
     *
     * <p>El archivo debe existir y pedir sus tres valores al entorno. Un
     * {@code ${DGT_DB_PASSWORD:cambiame}} pasaría este test y sería igual de peligroso, así
     * que también se exige que <strong>no haya valor por defecto</strong>.
     */
    @Test
    @DisplayName("application-prod.yml pide sus secretos al entorno, sin valores por defecto")
    void prodUsaPlaceholdersSinDefecto() throws IOException {
        Path prod = Path.of("src", "main", "resources", "application-prod.yml");
        assertThat(prod)
                .as("TODO_1: el perfil prod debe existir y externalizar la conexión")
                .isRegularFile();

        // Se juzga la CONFIGURACIÓN, no la prosa: las líneas de comentario se descartan.
        // (La primera versión de este test se cazó a sí misma: el comentario del YAML
        // explica por qué `${VAR:defecto}` es peligroso, y el test leyó el ejemplo.
        // Confundir la forma del texto con la propiedad es el defecto A-01 del proyecto.)
        String configuracion = Files.readAllLines(prod).stream()
                .filter(linea -> !linea.trim().startsWith("#"))
                .reduce("", (a, b) -> a + "\n" + b);

        assertThat(configuracion)
                .as("los tres valores llegan por variable de entorno")
                .contains("${DGT_DB_URL}", "${DGT_DB_USER}", "${DGT_DB_PASSWORD}");
        assertThat(configuracion)
                .as("un valor por defecto deja arrancar en silencio contra la base equivocada")
                .doesNotContain("${DGT_DB_URL:", "${DGT_DB_USER:", "${DGT_DB_PASSWORD:");
    }
}
