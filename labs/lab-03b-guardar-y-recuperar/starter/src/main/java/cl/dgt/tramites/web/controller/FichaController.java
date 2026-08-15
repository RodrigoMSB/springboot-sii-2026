package cl.dgt.tramites.web.controller;

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

    public FichaController(FichaService fichas) {
        this.fichas = fichas;
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
}
