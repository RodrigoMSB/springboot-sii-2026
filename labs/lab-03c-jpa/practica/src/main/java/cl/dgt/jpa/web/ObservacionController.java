package cl.dgt.jpa.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Lo mismo que viste en la consola, ahora por HTTP. Es el paso 10.
 *
 * <p>Los tres métodos están declarados y vacíos, igual que las demos. Como el controller también
 * necesita el repositorio, hay que recibirlo por constructor — lo mismo que hiciste en
 * {@code DemosJpa}.
 *
 * <p>Fíjate en los tipos de retorno: son comodines ({@code List<?>}, {@code ResponseEntity<?>},
 * {@code Map}) para que el proyecto compile antes de que exista la entidad. Al completarlos,
 * cámbialos por {@code Observacion} — es parte del trabajo.
 *
 * <p>Este controller devuelve la entidad directamente, sin DTO. Es a propósito: separar la
 * entidad de lo que sale por la API es tema de otro laboratorio, y aquí sumaría una capa que
 * distrae.
 */
@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    /**
     * Todas, o las de un autor si viene {@code ?autor=}. El segundo caso usa la consulta
     * derivada del paso 5: el mismo método que ya probaste en consola.
     */
    @GetMapping
    public List<?> listar(@RequestParam(required = false) String autor) {
        // escribe aquí
        return List.of();
    }

    /** Una sola. Si no está, 404 — para eso el repositorio devuelve {@code Optional}. */
    @GetMapping("/{id}")
    public ResponseEntity<?> porId(@PathVariable Long id) {
        // escribe aquí
        return ResponseEntity.notFound().build();
    }

    /** Crea una. Responde 201 con la observación creada, ya con su id. */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> nueva) {
        // escribe aquí
        return ResponseEntity.status(501).build();
    }
}
