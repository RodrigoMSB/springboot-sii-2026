package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.DeclaracionService;
import cl.dgt.tramites.application.ListadoService;
import cl.dgt.tramites.application.TramiteService;
import cl.dgt.tramites.application.PagoService;
import cl.dgt.tramites.application.EmisionService;
import cl.dgt.tramites.application.ResultadoEmision;
import cl.dgt.tramites.application.FolioDto;
import cl.dgt.tramites.web.dto.CrearTramiteRequest;
import cl.dgt.tramites.web.dto.NuevaLineaF29Request;
import cl.dgt.tramites.web.dto.TotalF29Dto;
import cl.dgt.tramites.web.dto.PaginaDto;
import cl.dgt.tramites.web.dto.TramiteDto;
import cl.dgt.tramites.application.TramiteResumenDto;
import org.springframework.data.domain.Pageable;
import cl.dgt.tramites.domain.tipo.EstadoTramite;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final PagoService pago;
    private final DeclaracionService declaracion;

    public TramiteController(TramiteService servicio, ListadoService listado, EmisionService emision,
                             PagoService pago, DeclaracionService declaracion) {
        this.servicio = servicio;
        this.listado = listado;
        this.emision = emision;
        this.pago = pago;
        this.declaracion = declaracion;
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
     * Confirma el pago de un trámite contra Tesorería y lo mueve a PAGADO. Si TESO no responde
     * a tiempo, la API no se cuelga: devuelve 503 rápido y el trámite queda intacto.
     */
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PostMapping("/{id}/pago")
    public TramiteDto confirmarPago(@PathVariable Long id) {
        return pago.confirmarPago(id);
    }

    /**
     * Emite el folio de un trámite. Idempotente (RN-05): la primera vez responde 201 con el
     * folio nuevo; un reintento responde 200 con el MISMO folio, sin crear otro.
     *
     * <p>Emitir folios es acto de FUNCIONARIO. Un CONTRIBUYENTE autenticado que lo intente
     * recibe 403 (tiene credencial, pero no para esta bóveda), no 401.
     */
    @PreAuthorize("hasRole('FUNCIONARIO')")
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

    /**
     * Declara una línea del F29 del trámite. Es la ÚNICA escritura que mueve los totales que
     * {@code GET /api/v1/reportes/totales-por-periodo} sirve cacheados — y por eso
     * {@link DeclaracionService} los invalida al escribir.
     *
     * <p>Declarar es acto de FUNCIONARIO, como emitir folios: la misma doctrina del Lab 07.
     */
    @PreAuthorize("hasRole('FUNCIONARIO')")
    @PostMapping("/{id}/f29/lineas")
    @ResponseStatus(HttpStatus.CREATED)
    public TotalF29Dto declararLinea(@PathVariable Long id,
                                     @Valid @RequestBody NuevaLineaF29Request peticion) {
        return new TotalF29Dto(id, declaracion.declararLinea(id, peticion.codigo(), peticion.monto()));
    }
}
