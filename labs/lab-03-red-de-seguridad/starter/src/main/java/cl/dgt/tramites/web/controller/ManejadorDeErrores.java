package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.domain.exception.TransicionIlegalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

/**
 * Traduce excepciones de dominio a {@code ProblemDetail} (RFC 9457).
 *
 * <p>El dominio no sabe que existe HTTP: lanza {@link ContribuyenteNoEncontradoException}
 * y no un 404. Aquí, y solo aquí, esa ignorancia se convierte en un código de estado.
 *
 * <p>Depende de {@code domain.exception}, no de {@code domain.entity}: AU-01 vigila lo
 * segundo. Una excepción no lleva puntajes de riesgo dentro.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(ContribuyenteNoEncontradoException.class)
    public ProblemDetail noEncontrado(ContribuyenteNoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Contribuyente no encontrado");
        problema.setType(URI.create("https://dgt.cl/errores/contribuyente-no-encontrado"));
        problema.setProperty("rut", e.getRut());
        return problema;
    }

    @ExceptionHandler(TramiteNoEncontradoException.class)
    public ProblemDetail tramiteNoEncontrado(TramiteNoEncontradoException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problema.setTitle("Trámite no encontrado");
        problema.setType(URI.create("https://dgt.cl/errores/tramite-no-encontrado"));
        problema.setProperty("id", e.getId());
        return problema;
    }

    // TODO_1 — aquí falta el handler de validación.
    //
    // Cuando @Valid rechaza un request, Spring lanza MethodArgumentNotValidException. Sin un
    // handler, eso es un 500 (o un formato feo). Escribe uno que devuelva un ProblemDetail
    // 400 con título "Datos inválidos" y una propiedad `campos` que sea un mapa
    // {nombreDelCampo -> mensaje} con cada error. Los tests E1 exigen $.campos.tipo.
    //
    // Pista: e.getBindingResult().getFieldErrors() te da la lista; FieldError#getField() y
    // #getDefaultMessage() son lo que necesitas. // {{TODO_1}}

    // TODO_3 — aquí falta el handler de la transición ilegal.
    //
    // El dominio lanza TransicionIlegalException cuando alguien intenta un salto de estado
    // que la máquina no permite (p. ej. BORRADOR -> FOLIADO). Sin handler, eso es un 500.
    // Un error de negocio merece un CONTRATO: escribe un handler que devuelva un
    // ProblemDetail 409 con type "https://dgt.cl/errores/transicion-ilegal" y las propiedades
    // `origen` y `destino`. Los tests E3 exigen ese shape exacto.
    //
    // Pista: la excepción trae getOrigen() y getDestino(). // {{TODO_3}}

}
