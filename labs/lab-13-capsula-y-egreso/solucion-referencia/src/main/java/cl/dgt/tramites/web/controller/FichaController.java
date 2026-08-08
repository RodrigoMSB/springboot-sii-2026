package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.application.ConsolidadoDto;
import cl.dgt.tramites.application.ConsolidadoService;
import cl.dgt.tramites.application.FichaService;
import cl.dgt.tramites.web.dto.FichaContribuyenteDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * La ficha pública de un contribuyente. Versionado nativo: {@code /api/v1/...}.
 *
 * <p>Devuelve un {@link FichaContribuyenteDTO}, jamás la entidad. No depende de
 * {@code ..domain.entity..} — ni siquiera dentro de un genérico —, y hay un guardián
 * (AU-02) que lo hace imposible de olvidar.
 */
@RestController
@RequestMapping("/api/v1/contribuyentes")
@Tag(name = "Contribuyentes", description = "Consulta de fichas públicas")
public class FichaController {

    private final FichaService fichas;
    private final ConsolidadoService consolidados;

    public FichaController(FichaService fichas, ConsolidadoService consolidados) {
        this.fichas = fichas;
        this.consolidados = consolidados;
    }

    @Operation(summary = "Ficha pública de un contribuyente",
            description = "Devuelve solo los campos públicos. El puntaje de riesgo interno "
                    + "jamás sale por esta API (RN-03).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha encontrada",
                    content = @Content(schema = @Schema(implementation = FichaContribuyenteDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe el contribuyente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{rut}/ficha")
    public ResponseEntity<FichaContribuyenteDTO> ficha(@PathVariable String rut) {
        return ResponseEntity.ok(fichas.fichaDe(rut));
    }

    /**
     * El consolidado del contribuyente: sus trámites, el estado de cada uno y el total declarado
     * del período. Lo pidió Carolina en el brief del examen de egreso.
     *
     * <p><strong>FISCALIZADOR, y solo FISCALIZADOR.</strong> El brief decía «para los
     * fiscalizadores», y eso es un requisito de seguridad, no una nota al margen: un consolidado
     * reúne en una sola respuesta lo que hasta ahora estaba disperso, y agregar datos es aumentar
     * su valor para quien no debería verlos. La doctrina del Lab 07: se cierra por defecto y se
     * abre nominalmente.
     *
     * <p><strong>El batch nocturno usa este mismo endpoint</strong>, con un token de servicio con
     * rol FISCALIZADOR. El brief no lo especificaba; se decidió así para no mantener dos caminos
     * hacia el mismo dato — dos caminos son dos sitios donde arreglar el próximo bug, y solo uno
     * se acuerda de arreglar quien lo encuentra.
     */
    @PreAuthorize("hasRole('FISCALIZADOR')")
    @GetMapping("/{rut}/consolidado")
    public ConsolidadoDto consolidado(@PathVariable String rut, @RequestParam String periodo) {
        return consolidados.consolidar(rut, periodo);
    }
}
