package cl.dgt.tramites.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Nuestra respuesta paginada. NO devolvemos el {@code Page} de Spring directamente: su forma
 * es un detalle interno (trae {@code pageable}, {@code sort}, campos que atan a quien consume
 * a nuestra librería). Exponemos un contrato propio, estable, que podemos cambiar por dentro.
 */
public record PaginaDto<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    public static <T> PaginaDto<T> de(Page<T> page) {
        return new PaginaDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
