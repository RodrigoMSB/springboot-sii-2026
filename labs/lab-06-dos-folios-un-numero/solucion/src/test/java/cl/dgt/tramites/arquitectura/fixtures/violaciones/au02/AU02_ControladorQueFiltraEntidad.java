package cl.dgt.tramites.arquitectura.fixtures.violaciones.au02;

import cl.dgt.tramites.domain.entity.Contribuyente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Viola AU-02 de la forma MÁS DIFÍCIL de cazar: la entidad aparece únicamente en el
 * parámetro de tipo genérico del retorno. El cuerpo devuelve null; en el bytecode el
 * descriptor del método solo menciona ResponseEntity.
 *
 * Una regla escrita sobre haveRawReturnType pasaría en verde. Ese es el punto.
 */
@RestController
public class AU02_ControladorQueFiltraEntidad {

    @GetMapping("/fixture/contribuyentes")
    public ResponseEntity<Contribuyente> filtrar() {
        return null;
    }
}
