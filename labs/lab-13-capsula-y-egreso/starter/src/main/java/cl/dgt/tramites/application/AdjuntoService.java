package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Adjunto;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.ArchivoInvalidoException;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.infrastructure.archivo.DetectorDeMime;
import cl.dgt.tramites.infrastructure.repository.AdjuntoRepository;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.AdjuntoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda y entrega adjuntos, con DESCONFIANZA (M11). No cree en la extensión ni en el
 * {@code Content-Type} declarado: juzga el archivo por su contenido, y sanea su nombre.
 */
@Service
public class AdjuntoService {

    private static final long TAMANO_MAXIMO = 5L * 1024 * 1024;   // 5 MB

    private final TramiteRepository tramites;
    private final AdjuntoRepository adjuntos;
    private final Path directorio;

    public AdjuntoService(TramiteRepository tramites, AdjuntoRepository adjuntos,
                          @Value("${dgt.adjuntos.dir}") String directorio) {
        this.tramites = tramites;
        this.adjuntos = adjuntos;
        this.directorio = Path.of(directorio);
    }

    @Transactional
    public AdjuntoDto subir(Long tramiteId, MultipartFile archivo) {
        Tramite tramite = tramites.findById(tramiteId)
                .orElseThrow(() -> new TramiteNoEncontradoException(tramiteId));

        if (archivo.isEmpty() || archivo.getSize() > TAMANO_MAXIMO) {
            throw new ArchivoInvalidoException("El archivo está vacío o supera los 5 MB.");
        }

        byte[] cabecera = leerCabecera(archivo);
        String mimeReal = DetectorDeMime.detectar(cabecera);      // rechaza si no es de la lista blanca
        String nombreSaneado = sanearNombre(archivo.getOriginalFilename());

        Adjunto guardado = adjuntos.save(new Adjunto(tramite, nombreSaneado, mimeReal));
        escribirEnDisco(guardado.getId(), archivo);
        return new AdjuntoDto(guardado.getId(), guardado.getNombreArchivo(), guardado.getMimeReal());
    }

    /**
     * Abre el contenido para descargar SIN cargar el archivo entero en memoria (un flujo). Devuelve
     * un {@link ContenidoAdjunto} —no la entidad— para que la web no la toque (AU-01).
     */
    @Transactional(readOnly = true)
    public ContenidoAdjunto descargar(Long adjuntoId) {
        Adjunto adjunto = adjuntos.findById(adjuntoId)
                .orElseThrow(() -> new ArchivoInvalidoException("No existe el adjunto " + adjuntoId));
        try {
            InputStream flujo = Files.newInputStream(rutaDe(adjunto.getId()));
            return new ContenidoAdjunto(adjunto.getNombreArchivo(), adjunto.getMimeReal(), flujo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // --- interno ---------------------------------------------------------------

    private byte[] leerCabecera(MultipartFile archivo) {
        try (InputStream in = archivo.getInputStream()) {
            return in.readNBytes(16);   // los primeros bytes bastan para los magic bytes
        } catch (IOException e) {
            throw new ArchivoInvalidoException("No pude leer el archivo.");
        }
    }

    /**
     * Neutraliza el path traversal: se queda con el ÚLTIMO segmento (tras cualquier {@code /} o
     * {@code \}), así {@code ../../etc/passwd} queda en {@code passwd}. Sin barras, no hay escape.
     * Y quita caracteres de control (incluido el byte nulo), que no tienen nada que hacer en un nombre.
     */
    private String sanearNombre(String original) {
        if (original == null || original.isBlank()) return "adjunto";
        String base = original.replaceAll(".*[/\\\\]", "");   // todo hasta la última barra, fuera
        base = base.replaceAll("[\\x00-\\x1f]", "");          // sin caracteres de control
        return base.isBlank() ? "adjunto" : base;
    }

    private void escribirEnDisco(Long id, MultipartFile archivo) {
        try {
            Files.createDirectories(directorio);
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, rutaDe(id), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path rutaDe(Long id) {
        return directorio.resolve(id + ".bin");
    }
}
