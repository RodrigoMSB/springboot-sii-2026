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
 *
 * <p><strong>Cómo elige Spring entre los cuatro handlers:</strong> gana el MÁS ESPECÍFICO, no el
 * que esté primero. Una {@code ProductoNoEncontradoException} encaja tanto en su handler como en
 * el de {@code Exception}, y se lleva el suyo porque es el tipo más cercano. Por eso el orden en
 * el archivo es solo para quien lee — pero conviene que sea de más concreto a más general, que es
 * como se razona.
 *
 * <p>Y un aviso que ahorra media hora: si un método lanza algo y <strong>no</strong> hay handler
 * que lo acepte, no falla nada visible — Spring devuelve su respuesta de error por defecto y
 * parece que el advice no existe.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    // El logger de esta clase. Se pide con la clase como argumento para que cada
    // línea salga con su nombre delante, y así se pueda subir o bajar el nivel de
    // este archivo solo desde `application.yml`. `static final` porque es uno
    // para todas las peticiones y no cambia.
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
        // La excepción llega como parámetro: Spring la inyecta al llamar al
        // handler. `getMessage()` es el texto que se armó en su constructor, ya
        // escrito para quien llama — por eso se puede devolver tal cual, y este
        // es el ÚNICO caso de los cuatro en que eso es seguro.
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
        // `getBindingResult()` es el informe de la validación, y dentro viene la
        // lista COMPLETA de campos que fallaron — no solo el primero. Se pasa a
        // un mapa campo -> motivo para que la respuesta diga qué arreglar.
        // `getField()` es el nombre del componente del record; `getDefaultMessage()`,
        // el texto que se escribió en el `message` de cada anotación.
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
        // El mensaje es fijo y no sale de la excepción: el suyo diría «No static
        // resource noexiste», que habla de recursos estáticos y confunde más que
        // ayuda a quien solo escribió mal una URL.
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
        // La excepción entera va al log, con su traza: pasarla como SEGUNDO
        // argumento —y no concatenada al texto— es lo que hace que el logger
        // imprima el stacktrace completo.
        log.error("Error no previsto atendiendo una petición", e);
        // Y al cliente, una frase fija. Nunca `e.getMessage()`: ese texto lo
        // escribió una librería para un programador, y puede nombrar tablas,
        // rutas de archivo o versiones. La información no se pierde, cambia de
        // destinatario.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorRespuesta.de("Ocurrió un error inesperado. Inténtalo más tarde.", 500));
    }
}
