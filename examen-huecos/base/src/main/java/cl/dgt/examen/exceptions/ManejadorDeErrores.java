package cl.dgt.examen.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorDeErrores {

    // =========================================================================
    //  HUECO 10 · La oficina que no existe
    // -------------------------------------------------------------------------
    //  Cuando el servicio lanza `OficinaNoEncontrada`, hoy el cliente recibe un 500.
    //  Un codigo que no existe no es una averia del servidor.
    //
    //  Tiene que salir el estado que corresponde, y con CUERPO: el mensaje del error
    //  y el codigo que se pidio. El test dice como se llama cada campo.
    //
    //  Al lado tienes resuelto el mismo caso para `SolicitudNoEncontrada`.
    //
    //  ESTA LISTO CUANDO · pasa el test H-10
    // =========================================================================

    @ExceptionHandler(SolicitudNoEncontrada.class)
    public ResponseEntity<Map<String, Object>> solicitudNoEncontrada(SolicitudNoEncontrada e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "id", e.getId()));
    }

    // =========================================================================
    //  HUECO 11 · El cuerpo que llega mal
    // -------------------------------------------------------------------------
    //  Cuando un cuerpo no pasa la validacion, Spring ya responde 400 — pero el que
    //  lo mando no se entera de QUE campo venia mal.
    //
    //  Tiene que salir un 400 con el detalle: un mapa `campos` con el nombre de cada
    //  campo invalido y su mensaje. El test mira `campos.estado`.
    //
    //  ESTA LISTO CUANDO · pasa el test H-11
    // =========================================================================
}
