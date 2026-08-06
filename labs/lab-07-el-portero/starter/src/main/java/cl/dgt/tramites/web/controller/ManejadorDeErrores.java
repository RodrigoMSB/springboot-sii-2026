package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.domain.exception.TramiteNoEncontradoException;
import cl.dgt.tramites.domain.exception.TransicionIlegalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Uno o más campos no son válidos");
        problema.setTitle("Datos inválidos");
        problema.setType(URI.create("https://dgt.cl/errores/datos-invalidos"));
        // El detalle NOMBRA cada campo inválido con su mensaje: un cliente puede corregir
        // sin adivinar. `campo -> mensaje`, ordenado, determinístico.
        Map<String, String> campos = new TreeMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail credencialesInvalidas(AuthenticationException e) {
        // 401 GENÉRICO: no distingue "no existe el usuario" de "clave incorrecta". Distinguir
        // le regalaría a un atacante qué RUT están registrados. El detalle es idéntico siempre.
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas");
        problema.setTitle("No autenticado");
        problema.setType(URI.create("https://dgt.cl/errores/credenciales-invalidas"));
        return problema;
    }

    @ExceptionHandler(TransicionIlegalException.class)
    public ProblemDetail transicionIlegal(TransicionIlegalException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problema.setTitle("Transición de estado ilegal");
        problema.setType(URI.create("https://dgt.cl/errores/transicion-ilegal"));
        problema.setProperty("origen", e.getOrigen().name());
        problema.setProperty("destino", e.getDestino().name());
        return problema;
    }
}
