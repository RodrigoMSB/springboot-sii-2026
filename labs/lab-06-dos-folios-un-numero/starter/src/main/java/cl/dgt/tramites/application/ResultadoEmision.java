package cl.dgt.tramites.application;

/**
 * El resultado de emitir: el folio, y si fue reci&eacute;n {@code creado} o {@code reusado}.
 *
 * <p>El controlador lo traduce a HTTP: {@code creado} &rarr; 201, {@code reusado} &rarr; 200.
 * Ese bit es toda la se&ntilde;al que RN-05 (idempotencia) necesita: un reintento no crea nada,
 * devuelve lo mismo con 200.
 */
public record ResultadoEmision(FolioDto folio, boolean creado) {

    public static ResultadoEmision nuevo(FolioDto folio) {
        return new ResultadoEmision(folio, true);
    }

    public static ResultadoEmision reusado(FolioDto folio) {
        return new ResultadoEmision(folio, false);
    }
}
