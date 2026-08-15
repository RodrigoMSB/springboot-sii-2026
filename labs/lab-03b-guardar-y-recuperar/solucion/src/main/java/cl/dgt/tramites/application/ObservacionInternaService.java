package cl.dgt.tramites.application;

import cl.dgt.tramites.domain.entity.Contribuyente;
import cl.dgt.tramites.domain.entity.ObservacionInterna;
import cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Guardar una observación interna y recuperarla.
 *
 * <p>Fíjate en lo que NO hay en este archivo: ni una conexión, ni un {@code INSERT}, ni un
 * {@code SELECT}, ni un mapeo de columnas. Solo objetos. El SQL lo escribe Hibernate a partir
 * de lo que declaraste en la entidad, y puedes verlo con
 * {@code ./bin/start-lab.sh --ver-sql}.
 *
 * <p>{@code @Transactional(readOnly = true)} en la lectura no es decoración: la relación con el
 * contribuyente es LAZY, y leer su RUT necesita que la sesión de persistencia siga abierta.
 */
@Service
@Profile("dev")
public class ObservacionInternaService {

    private final ObservacionInternaRepository observaciones;
    private final ContribuyenteRepository contribuyentes;

    public ObservacionInternaService(ObservacionInternaRepository observaciones,
                                     ContribuyenteRepository contribuyentes) {
        this.observaciones = observaciones;
        this.contribuyentes = contribuyentes;
    }

    /**
     * Guarda una observación nueva. Un objeto entra, una fila aparece.
     *
     * <p>El {@code id} lo pone el motor y Hibernate lo escribe de vuelta en el objeto: la
     * instancia que devuelve {@code save} ya lo trae.
     */
    @Transactional
    public ObservacionInternaVista guardar(String rut, String texto, String autor) {
        Contribuyente contribuyente = contribuyentes.findByRut(rut)
                .orElseThrow(() -> new ContribuyenteNoEncontradoException(rut));

        ObservacionInterna guardada =
                observaciones.save(new ObservacionInterna(contribuyente, texto, autor));

        return new ObservacionInternaVista(
                rut, guardada.getTexto(), guardada.getAutor(), guardada.getCreadaEn());
    }

    /** Todas las observaciones de un contribuyente. */
    @Transactional(readOnly = true)
    public List<ObservacionInternaVista> porRut(String rut) {
        return observaciones.findByContribuyenteRut(rut).stream()
                .map(o -> new ObservacionInternaVista(
                        o.getContribuyente().getRut(), o.getTexto(), o.getAutor(), o.getCreadaEn()))
                .toList();
    }
}
