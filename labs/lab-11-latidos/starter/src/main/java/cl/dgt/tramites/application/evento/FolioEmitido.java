package cl.dgt.tramites.application.evento;

/**
 * «Se emitió un folio.» Un hecho del pasado, no una orden.
 *
 * <p>Esa distinción es todo el punto de los eventos. Si {@code EmisionService} llamara directo al
 * notificador, estaría <em>mandando</em>: «notifica». Publicando un hecho, dice «esto ocurrió» y se
 * desentiende de quién quiera enterarse. Mañana se agrega un listener que actualiza un tablero, y
 * otro que archiva para el fiscalizador, y {@code EmisionService} no se toca.
 *
 * <p>Es un {@code record} inmutable y sin comportamiento: un evento que se puede modificar mientras
 * viaja entre listeners es una variable global con mejor prensa.
 *
 * @param tramiteId el trámite al que se le emitió folio
 * @param numero    el número de folio emitido
 * @param rut       el RUT del contribuyente a quien avisar
 */
public record FolioEmitido(Long tramiteId, long numero, String rut) {
}
