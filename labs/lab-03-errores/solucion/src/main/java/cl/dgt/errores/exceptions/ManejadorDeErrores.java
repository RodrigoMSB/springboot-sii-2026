package cl.dgt.errores.exceptions;

import cl.dgt.errores.dto.ErrorRespuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

// Un solo sitio para los errores de todos los controllers.
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(ProductoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de(e.getMessage(), 404));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

        // Constructor completo y no el atajo `de(...)`: este es el único error
        // que sí tiene detalle por campo.
        ErrorRespuesta cuerpo = new ErrorRespuesta(
                "Hay datos inválidos en la petición.", 400, Instant.now(), campos);
        // `badRequest()` es el atajo de `status(HttpStatus.BAD_REQUEST)`: 400.
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaNoExiste(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de("La ruta pedida no existe.", 404));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> todoLoDemas(Exception e) {
        log.error("Error no previsto atendiendo una petición", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.de("Ocurrió un error inesperado. Inténtalo más tarde.", 500));
    }
}
