package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ListadoService;
import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.application.EmisionService;
import cl.dgt.tramites.application.ResultadoEmision;
import cl.dgt.tramites.application.FolioDto;
import cl.dgt.tramites.web.dto.CrearTramiteRequest;
import cl.dgt.tramites.web.dto.PaginaDto;
import cl.dgt.tramites.web.dto.TramiteDto;
import cl.dgt.tramites.application.TramiteResumenDto;
import org.springframework.data.domain.Pageable;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** El primer endpoint que escribe el alumno. Devuelve DTO; jamás la entidad. */
@RestController
@RequestMapping("/api/v1/tramites")
public class TramiteController {

    private final TramiteService servicio;
    private final ListadoService listado;
    private final EmisionService emision;

    public TramiteController(TramiteService servicio, ListadoService listado, EmisionService emision) {
        this.servicio = servicio;
        this.listado = listado;
        this.emision = emision;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramiteDto> porId(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.buscarPorId(id));
    }

    /** Crea un trámite. El @Valid dispara la validación declarativa del request. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TramiteDto crear(@Valid @RequestBody CrearTramiteRequest peticion) {
        return servicio.crear(peticion.rutContribuyente(), peticion.tipo());
    }

    /** Avanza el trámite. Una transición ilegal termina en 409 (ver ManejadorDeErrores). */
    @PostMapping("/{id}/avanzar")
    public TramiteDto avanzar(@PathVariable Long id, @RequestParam EstadoTramite a) {
        return servicio.avanzar(id, a);
    }

    /**
     * Emite el folio de un trámite. Idempotente (RN-05): la primera vez responde 201 con el
     * folio nuevo; un reintento responde 200 con el MISMO folio, sin crear otro.
     */
    @PostMapping("/{id}/folio")
    public ResponseEntity<FolioDto> emitirFolio(@PathVariable Long id) {
        ResultadoEmision r = emision.emitir(id);
        HttpStatus estado = r.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(estado).body(r.folio());
    }

    /** El listado paginado. Rápido: una proyección, sin arrastrar el árbol de cada trámite. */
    @GetMapping
    public PaginaDto<TramiteResumenDto> listar(Pageable pagina) {
        return listado.listar(pagina);
    }
}
