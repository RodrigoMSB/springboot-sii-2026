package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Adjunto;
import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
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
 * Guarda adjuntos… CONFIANDO en el cliente. TODO_4:
 *   · el tipo lo toma del {@code Content-Type} que el cliente declara (mentira fácil): un .exe
 *     disfrazado de .pdf entra sin problemas.
 *   · el nombre lo guarda tal cual: un {@code ../../etc/passwd} pasa sin sanear.
 * Debe juzgar por el CONTENIDO (magic bytes) y sanear el nombre.
 */
@Service
public class AdjuntoService {

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

        String mimeDeclarado = archivo.getContentType();       // el cliente MIENTE, y le creemos
        String nombreCrudo = archivo.getOriginalFilename();    // sin sanear: path traversal pasa

        Adjunto guardado = adjuntos.save(new Adjunto(tramite, nombreCrudo, mimeDeclarado));
        escribirEnDisco(guardado.getId(), archivo);
        return new AdjuntoDto(guardado.getId(), guardado.getNombreArchivo(), guardado.getMimeReal());
    }

    @Transactional(readOnly = true)
    public ContenidoAdjunto descargar(Long adjuntoId) {
        Adjunto adjunto = adjuntos.findById(adjuntoId).orElseThrow();
        try {
            InputStream flujo = Files.newInputStream(directorio.resolve(adjuntoId + ".bin"));
            return new ContenidoAdjunto(adjunto.getNombreArchivo(), adjunto.getMimeReal(), flujo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void escribirEnDisco(Long id, MultipartFile archivo) {
        try {
            Files.createDirectories(directorio);
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, directorio.resolve(id + ".bin"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
