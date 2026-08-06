package cl.dgt.tramites.application;

import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import cl.dgt.tramites.web.dto.PaginaDto;
import cl.dgt.tramites.application.TramiteResumenDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El listado de trámites. Esta es la versión RÁPIDA: una proyección paginada, una consulta.
 *
 * <p>{@code @Transactional(readOnly = true)}: el listado no escribe nada. Marca la transacción
 * de solo lectura — Hibernate puede saltarse el dirty-checking, y el intento queda declarado.
 * (M7: el proxy transaccional. Aquí aplicado, señalado, no tecleado por ti.)
 */
@Service
public class ListadoService {

    private final TramiteRepository repositorio;

    public ListadoService(TramiteRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public PaginaDto<TramiteResumenDto> listar(Pageable pagina) {
        return PaginaDto.de(repositorio.resumenPaginado(pagina));
    }
}
