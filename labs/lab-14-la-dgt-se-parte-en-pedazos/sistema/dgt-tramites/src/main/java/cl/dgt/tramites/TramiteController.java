package cl.dgt.tramites;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * La API de trámites. Dos endpoints, y los dos componen datos de dos servicios.
 */
@RestController
@RequestMapping("/api/v1/tramites")
public class TramiteController {

    private final TramiteRepository repositorio;
    private final ConsultaDeContribuyentes contribuyentes;

    public TramiteController(TramiteRepository repositorio, ConsultaDeContribuyentes contribuyentes) {
        this.repositorio = repositorio;
        this.contribuyentes = contribuyentes;
    }

    @GetMapping
    public List<TramiteDto> listar() {
        return repositorio.findAll().stream().map(this::componer).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramiteDto> porId(@PathVariable Long id) {
        return repositorio.findById(id)
                .map(this::componer)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * La composición: lo local, más lo remoto.
     *
     * <p>Este método es donde se nota que el sistema está partido. La mitad de
     * los campos salen de una consulta a la base propia, que o funciona o lanza.
     * La otra mitad sale de una llamada de red que puede devolver
     * {@code razonSocial == null} sin avisar a nadie.
     *
     * <p>Y lo que se devuelve es un 200 con un JSON completo en la forma e
     * incompleto en el contenido. Nada en la respuesta dice «esto está
     * degradado». Ese silencio es el crimen del laboratorio, y arreglarlo es el
     * desafío {@code 99-}.
     */
    private TramiteDto componer(Tramite t) {
        FichaContribuyente ficha = contribuyentes.buscar(t.getRutContribuyente());
        return new TramiteDto(
                t.getId(),
                t.getTipo(),
                t.getEstado(),
                t.getRutContribuyente(),
                ficha.razonSocial(),
                ficha.atendidoPor());
    }
}
