package cl.dgt.rendimiento.dto;

/**
 * Exactamente lo que la pantalla necesita mostrar, y nada más.
 *
 * <p>No es una entidad: no está mapeada a ninguna tabla, no la vigila Hibernate y no tiene
 * relaciones que se puedan tocar por accidente. Es un dato de salida — llegó en el paso 4.
 *
 * @param rut               el RUT del contribuyente
 * @param razonSocial       su razón social
 * @param cuantosTramites   cuántos trámites tiene, contados por la base
 */
public record ResumenContribuyente(String rut, String razonSocial, long cuantosTramites) {
}
