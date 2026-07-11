package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.PaginaDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El listado — versión CON N+1. Es funcionalmente idéntica a la solución: devuelve
 * exactamente los mismos datos. Y cuesta una consulta por cada trámite de la página, porque
 * itera las entidades y toca `getContribuyente().getRut()` una por una.
 *
 * <p>Esta clase existe a propósito (P-16): convive con la solución para que el contador
 * demuestre lo que el ojo no ve. Pasa E2 (funcional). Falla E1 (contador). Ese contraste ES
 * la definición ejecutable de optimizar.
 */
@Service
public class ListadoService {

    private final TramiteRepository repositorio;

    public ListadoService(TramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PaginaDto<TramiteResumenDto> listar(Pageable pagina) {
        // findAll trae ENTIDADES; el map toca cada contribuyente => N+1 (y el @OneToOne
        // inverso de cada trámite suma aún más: LAZY que Hibernate ignora. Se mide, no se cree).
        return PaginaDto.de(repositorio.findAll(pagina).map(t -> new TramiteResumenDto(
                t.getId(), t.getTipo(), t.getEstado().name(), t.getContribuyente().getRut())));
    }
}
