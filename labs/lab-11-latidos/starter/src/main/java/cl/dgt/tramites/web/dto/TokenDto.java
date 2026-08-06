package cl.dgt.tramites.web.dto;

/** Lo que devuelve el login: el JWT firmado y cómo usarlo. */
public record TokenDto(String token, String tipo, long expiraEnSegundos) {
}
