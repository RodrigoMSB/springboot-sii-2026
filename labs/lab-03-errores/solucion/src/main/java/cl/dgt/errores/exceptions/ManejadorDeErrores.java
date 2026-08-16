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

/**
 * El traductor de excepciones a respuestas HTTP, para toda la aplicación.
 *
 * <p>{@code @RestControllerAdvice} significa «esto vale para todos los controllers». No hay que
 * registrarlo en ninguna parte ni llamarlo desde ningún sitio: cuando un método de un controller
 * lanza algo, Spring busca aquí un {@code @ExceptionHandler} que lo acepte.
 *
 * <p>Por eso el controller no menciona ni un código HTTP: lanza lo que pasó, y la traducción vive
 * en un solo sitio.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    // =========================================================================
    //  404 · EL CASO PREVISTO
    // -------------------------------------------------------------------------
    //  La excepción trae el mensaje ya escrito para quien llama; aquí solo se le
    //  pone el código y la forma. Llegó en el paso 3.
    //  Qué se espera ver: 404 con cuerpo JSON, y ni una línea de traza.
    //  Para pensar: ¿cuántos controllers habría que tocar para cambiar este
    //  formato en toda la API? (Cero. Este archivo.)
    // =========================================================================
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(ProductoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de(e.getMessage(), 404));
    }

    // =========================================================================
    //  400 · LO QUE MANDÓ MAL QUIEN LLAMA
    // -------------------------------------------------------------------------
    //  Spring lanza esta excepción cuando un @Valid falla, y dentro trae la lista
    //  completa de campos que no cumplieron. Aquí se pasa a un mapa
    //  campo -> motivo, para que la respuesta diga qué arreglar. Llegó en el paso 4.
    //  Qué se espera ver: 400 con "campos": {"nombre": "...", "precio": "..."}.
    //  Para pensar: ¿por qué se devuelven TODOS los campos malos y no solo el primero?
    // =========================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

        ErrorRespuesta cuerpo = new ErrorRespuesta(
                "Hay datos inválidos en la petición.", 400, Instant.now(), campos);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    // =========================================================================
    //  404 · UNA RUTA QUE NO EXISTE
    // -------------------------------------------------------------------------
    //  Este handler NO estaba previsto: hizo falta por culpa del de abajo. Al
    //  atrapar Exception se atrapa también el aviso de Spring de que la URL no
    //  corresponde a nada, y una dirección mal escrita acababa devolviendo 500.
    //  Se descubrió midiendo, y el paso 5 lo reproduce.
    //  Qué se espera ver: /noexiste da 404, no 500.
    //  Para pensar: ¿qué más estará atrapando de más el handler de abajo?
    // =========================================================================
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaNoExiste(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorRespuesta.de("La ruta pedida no existe.", 404));
    }

    // =========================================================================
    //  500 · TODO LO DEMÁS
    // -------------------------------------------------------------------------
    //  La red de seguridad. Atrapa lo que nadie previó y devuelve SIEMPRE lo
    //  mismo: una frase sin información. Lo que sí se cuenta entero va al log,
    //  que es de la casa. Llegó en el paso 5.
    //  Qué se espera ver: 500 con un mensaje genérico, y la traza completa en la
    //  consola del servidor.
    //  Para pensar: ¿qué le regalas a un atacante si el mensaje interno sale por
    //  la API?
    // =========================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> todoLoDemas(Exception e) {
        log.error("Error no previsto atendiendo una petición", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.de("Ocurrió un error inesperado. Inténtalo más tarde.", 500));
    }
}
