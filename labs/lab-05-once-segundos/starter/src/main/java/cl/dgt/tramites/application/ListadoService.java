package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.PaginaDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El listado de trámites.
 *
 * <p>⚠️ Funciona. Devuelve los datos correctos. Y cuesta una consulta por cada trámite de la
 * página, más las que el @OneToOne inverso arrastra. Con la semilla normal, ni lo notas. Con
 * `start-lab.sh --lotes 50000`, cuenta los segundos.
 *
 * <p><strong>TODO_1 — hazlo volar sin cambiar lo que devuelve (≈15 min).</strong> El test
 * {@code E1_ContadorDeConsultasIT} exige que una página cueste ≤ 3 consultas, sin importar
 * cuántos trámites haya. Iterar entidades no lo logra: hay que traer SOLO lo que la tabla
 * pinta, en una consulta. Escribe una PROYECCIÓN paginada en {@code TramiteRepository}
 * ({@code @Query} con {@code SELECT new ...TramiteResumenDto(...)} y un JOIN al contribuyente)
 * y haz que este servicio la use en vez de {@code findAll}.
 *
 * <p>Pista: `E2_ListadoFuncionalIT` no debe cambiar. Eso es refactorizar: mismo comportamiento,
 * distinto costo. La pista del Lab 04 se cobra aquí — esta es la pregunta que no te habías hecho.
 */
@Service
public class ListadoService {

    private final TramiteRepository repositorio;

    public ListadoService(TramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PaginaDto<TramiteResumenDto> listar(Pageable pagina) {
        // El N+1: findAll trae entidades, y getContribuyente().getRut() carga una por una.
        return PaginaDto.de(repositorio.findAll(pagina).map(t -> new TramiteResumenDto(
                t.getId(), t.getTipo(), t.getEstado().name(), t.getContribuyente().getRut())));
    }
}
