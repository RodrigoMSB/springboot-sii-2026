package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Las observaciones internas de un contribuyente.
 *
 * <p>Compara este archivo con el {@code ReporteInternoLegacyDao} que había en su lugar: eran
 * cuarenta líneas de conexión, {@code Statement}, {@code ResultSet} y un {@code catch} vacío.
 * Aquí no hay conexión que abrir ni cerrar, no hay columnas que leer una por una, y no hay
 * ningún error que se pueda tragar en silencio: si la base falla, la excepción sube.
 *
 * <p>{@code @Transactional(readOnly = true)} porque el {@code @ManyToOne} es LAZY: leer el RUT
 * del contribuyente necesita que la sesión de persistencia siga abierta. Es la primera vez que
 * el curso topa con eso, y el Lab 04 lo convierte en tema.
 */
@Service
@Profile("dev")
public class ObservacionInternaService {

    private final ObservacionInternaRepository repositorio;

    public ObservacionInternaService(ObservacionInternaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<ObservacionInternaVista> porRut(String rut) {
        return repositorio.findByContribuyenteRut(rut).stream()
                .map(o -> new ObservacionInternaVista(
                        o.getContribuyente().getRut(), o.getTexto(), o.getAutor(), o.getCreadaEn()))
                .toList();
    }
}
