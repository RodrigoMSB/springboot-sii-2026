package cl.dgt.tramites.web.controller;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La ficha pública de un contribuyente.
 *
 * <p>⚠️ ESTE CÓDIGO ES EL CRIMEN. Lo dejó alguien con prisa. Funciona: compila, arranca,
 * responde. Y por el JSON viaja {@code puntajeRiesgoInterno}, el número con que la DGT decide
 * a quién fiscalizar. Nada lo impidió.
 *
 * <p>Tus TODOs 1 y 2 lo arreglan: devolver una ficha (DTO, lista blanca) en vez de la entidad,
 * y sacar la lógica a una capa de servicio. Pero no lo toques todavía — primero vive el crimen:
 *
 * <pre>
 *   ./bin/start-lab.sh
 *   curl http://localhost:8099/api/v1/contribuyentes/12345678-5/ficha
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/contribuyentes")
public class FichaController {

    // Olor nº 1: un controlador que habla directo con el repositorio. La capa de servicio
    // no existe. Olor nº 2 (el grave): devuelve la ENTIDAD. TODO_1 y TODO_2.
    private final ContribuyenteRepository repositorio;

    public FichaController(ContribuyenteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @GetMapping("/{rut}/ficha")
    public ResponseEntity<Contribuyente> ficha(@PathVariable String rut) {
        // "era para ayer, después lo arreglo"
        Contribuyente c = repositorio.findByRut(rut)
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rut));
        return ResponseEntity.ok(c);
    }
}
