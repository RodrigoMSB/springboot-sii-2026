package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.web.dto.TramiteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El primer endpoint que escribes tú.
 *
 * <p><strong>TODO_4 — {@code GET /api/tramites/{id}} (≈15 min).</strong>
 *
 * <p><em>Qué:</em> devuelve el trámite pedido como {@link TramiteDto}, con estado 200. Y
 * cuando el id no existe, responde <strong>404 con un {@code ProblemDetail}</strong>, no
 * con una traza de 300 líneas que le regala a un atacante los nombres de tus clases.
 *
 * <p><em>Por qué:</em> el camino feliz lo escribe cualquiera. Lo que distingue a un
 * profesional es lo que pasa cuando el dato no está. Un {@code ProblemDetail} (RFC 9457) es
 * un contrato: un cliente puede leerlo sin adivinar.
 *
 * <p><em>Y la regla que no se ve:</em> devuelves un DTO, jamás la entidad. Hay siete tests
 * de arquitectura vigilando; el {@code AU-02} caza la entidad incluso si viaja escondida
 * dentro de un genérico. Pruébalo si no me crees.
 *
 * <p>Pista 2: el servicio {@link TramiteService#buscarPorId(Long)} ya existe y ya lanza
 * {@code TramiteNoEncontradoException}. Te falta (a) devolver su resultado aquí, y (b)
 * enseñarle a {@code ManejadorDeErrores} a traducir esa excepción a un 404. Fíjate en cómo
 * lo hace ya para {@code ContribuyenteNoEncontradoException}: cópiate a ti mismo, no a
 * StackOverflow.
 */
@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteService servicio;

    public TramiteController(TramiteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramiteDto> porId(@PathVariable Long id) {
        throw new UnsupportedOperationException("{{TODO_4}}");
    }
}
