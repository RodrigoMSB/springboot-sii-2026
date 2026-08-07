package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.AdjuntoService;
import cl.dgt.tramites.application.ContenidoAdjunto;
import cl.dgt.tramites.web.dto.AdjuntoDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

/** Sube y descarga adjuntos. La descarga va en streaming: no carga el archivo entero en memoria. */
@RestController
@RequestMapping("/api/v1")
public class AdjuntoController {

    private final AdjuntoService servicio;

    public AdjuntoController(AdjuntoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping(value = "/tramites/{id}/adjuntos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AdjuntoDto subir(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) {
        return servicio.subir(id, archivo);
    }

    @GetMapping("/adjuntos/{id}/contenido")
    public ResponseEntity<StreamingResponseBody> descargar(@PathVariable Long id) {
        ContenidoAdjunto contenido = servicio.descargar(id);
        StreamingResponseBody cuerpo = salida -> {
            try (InputStream in = contenido.flujo()) {
                in.transferTo(salida);   // fluye de disco a la red, sin buffer gigante en memoria
            }
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + contenido.nombreArchivo() + "\"")
                .contentType(MediaType.parseMediaType(contenido.mimeReal()))
                .body(cuerpo);
    }
}
