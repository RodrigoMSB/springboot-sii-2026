// Convierte las excepciones en respuestas con forma. Llega hecho salvo la primera regla.
// Tu equivalente: el mismo archivo, cambiando qué excepción se traduce a 404.
package cl.dgt.consolidado.controllers;

import cl.dgt.consolidado.services.OficinaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(OficinaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> noEncontrada(OficinaNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
                       MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> parametroMalo(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Faltan `desde` y `hasta`, o no tienen formato YYYY-MM-DD"));
    }
}
// ^ LAS DOS REGLAS DEL ENCARGO, y las dos devuelven un cuerpo `{"mensaje": ...}`:
//
//     404  la oficina (tu contribuyente) no existe
//     400  falta un parámetro obligatorio, o la fecha no se puede parsear
//
//   Los dos tipos de excepción del segundo manejador son los que lanza Spring solo:
//   `MissingServletRequestParameterException` cuando falta el parámetro, y
//   `MethodArgumentTypeMismatchException` cuando llega pero no es una fecha. Capturar los dos
//   juntos da el mismo mensaje para los dos casos, que desde fuera son el mismo problema.
//
//   El mensaje es genérico A PROPÓSITO: no dice cuál de los dos faltaba. Un error de entrada no
//   tiene por qué describir el contrato entero, y aquí el contrato está en /swagger-ui.html.
