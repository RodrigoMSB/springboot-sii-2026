package cl.dgt.jpa.web;

import cl.dgt.jpa.entities.Observacion;
import cl.dgt.jpa.repositories.ObservacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lo mismo que viste en la consola, ahora por HTTP.
 *
 * <p>Es el mismo repositorio y son las mismas llamadas: cambia quién las dispara. Sirve para
 * comprobar desde Postman que el dato quedó de verdad — guardas con un POST, apagas el programa,
 * lo vuelves a levantar, y el GET lo sigue trayendo.
 *
 * <p>Devuelve la entidad directamente, sin DTO. Es a propósito: separar la entidad de lo que sale
 * por la API es tema de otro laboratorio, y aquí sumaría una capa que distrae. En un sistema real
 * no se hace — exponer la entidad ata la API a la forma de la tabla, y el Lab 02 del arco antiguo
 * existía justamente por lo que se filtra al hacerlo.
 *
 * <p>El repositorio llega por constructor, exactamente igual que en el Lab 02. Que sea una
 * interfaz sin implementación escrita no cambia nada: Spring Data la construyó al arrancar y la
 * dejó en el contenedor.
 */
@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    private final ObservacionRepository repositorio;

    public ObservacionController(ObservacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Todas, o las de un autor si viene {@code ?autor=}. El segundo caso usa la consulta
     * derivada del paso 5: el mismo método que ya probaste en consola.
     */
    @GetMapping
    public List<Observacion> listar(@RequestParam(required = false) String autor) {
        // `required = false` hace opcional el parámetro: sin él, pedir
        // /api/observaciones a secas daría 400. Cuando no viene llega `null`, y
        // ese null es el que decide cuál de las dos consultas se usa.
        return (autor == null) ? repositorio.findAll() : repositorio.findByAutor(autor);
    }

    /** Una sola. Si no está, 404 — para eso el repositorio devuelve {@code Optional}. */
    @GetMapping("/{id}")
    public ResponseEntity<Observacion> porId(@PathVariable Long id) {
        // El Optional que devuelve findById se traduce aquí a HTTP: lleno → 200
        // con la observación; vacío → 404. Es el mismo patrón del Lab 02, y la
        // razón por la que el repositorio devuelve Optional y no null.
        return repositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Crea una. Responde 201 con la observación creada, ya con su id. */
    @PostMapping
    public ResponseEntity<Observacion> crear(@RequestBody Observacion nueva) {
        // save() devuelve la observación YA con su id puesto por la base, y es
        // esa —no la que llegó— la que se devuelve: así quien llama se entera
        // del id sin tener que pedirlo después.
        // 201 y no 200 porque la petición creó algo.
        return ResponseEntity.status(HttpStatus.CREATED).body(repositorio.save(nueva));
    }
}
