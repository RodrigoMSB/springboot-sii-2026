package cl.dgt.tramites.application;

import java.io.InputStream;

/**
 * El contenido de un adjunto para descargar: su nombre, su tipo real, y un FLUJO (no los bytes en
 * memoria). Vive en {@code application} para que la web pueda entregarlo sin tocar la entidad (AU-01).
 */
public record ContenidoAdjunto(String nombreArchivo, String mimeReal, InputStream flujo) {
}
