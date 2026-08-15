package cl.dgt.tramites.web.dto;

/**
 * Lo que la DGT le cuenta al mundo sobre un contribuyente.
 *
 * <p>Fíjate en lo que NO está: {@code puntajeRiesgoInterno}. RN-03. El DTO no es
 * ceremonia: es la frontera donde se decide qué sale. Si un día alguien devuelve la
 * entidad "para no duplicar código", el folio se filtra — y AU-02 lo detiene antes.
 */
public record ContribuyenteDto(String rut, String razonSocial) {
}
