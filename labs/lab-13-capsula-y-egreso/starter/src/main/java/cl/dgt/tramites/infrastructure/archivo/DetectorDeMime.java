package cl.dgt.tramites.infrastructure.archivo;

import cl.dgt.tramites.domain.exception.ArchivoInvalidoException;

/**
 * Detecta el tipo REAL de un archivo por sus <em>magic bytes</em> —los primeros bytes del
 * contenido—, no por la extensión ni por el {@code Content-Type} que el cliente declara. Un
 * ejecutable renombrado a {@code .pdf} empieza con {@code MZ}, no con {@code %PDF}: se caza aquí.
 *
 * <p>Lista blanca: solo PDF e imágenes PNG/JPEG. Todo lo demás se rechaza. Confiar en la extensión
 * es confiar en el atacante para que se identifique.
 */
public final class DetectorDeMime {

    private DetectorDeMime() {
    }

    public static String detectar(byte[] cabecera) {
        if (empiezaCon(cabecera, 0x25, 0x50, 0x44, 0x46)) return "application/pdf";   // %PDF
        if (empiezaCon(cabecera, 0x89, 0x50, 0x4E, 0x47)) return "image/png";         // .PNG
        if (empiezaCon(cabecera, 0xFF, 0xD8, 0xFF))       return "image/jpeg";        // JPEG
        throw new ArchivoInvalidoException(
                "Tipo de archivo no permitido (se juzga por el contenido, no por el nombre).");
    }

    private static boolean empiezaCon(byte[] datos, int... magia) {
        if (datos.length < magia.length) return false;
        for (int i = 0; i < magia.length; i++) {
            if ((datos[i] & 0xFF) != magia[i]) return false;
        }
        return true;
    }
}
