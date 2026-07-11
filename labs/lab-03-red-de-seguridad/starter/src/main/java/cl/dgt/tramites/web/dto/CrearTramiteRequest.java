package cl.dgt.tramites.web.dto;

/**
 * Lo que llega en {@code POST /api/v1/tramites}.
 *
 * <p><strong>TODO_1 — la validación declarativa (≈15 min).</strong> Ahora mismo este record
 * no valida nada: un tipo en blanco o un RUT basura entran sin que nadie los detenga, y
 * revientan más adentro. Los tests {@code E1_ValidacionDelRequestTest} exigen que un request
 * inválido se rechace en la frontera con 400.
 *
 * <p><em>Qué escribir:</em> anota los campos con Bean Validation — {@code @NotBlank} donde
 * corresponda, {@code @Pattern} para el tipo (solo {@code DECLARACION_F29} o
 * {@code INICIO_ACTIVIDADES}), y {@code @RutValido} sobre el RUT (esa es la anotación del
 * TODO_2). Y en {@code ManejadorDeErrores} falta el handler que convierte una validación
 * fallida en un 400 que NOMBRA los campos: mira el {@code {{TODO_1}}} allí.
 *
 * <p>Pista: el {@code @Valid} en el controlador ya está puesto. Solo dispara si el record
 * tiene anotaciones que verificar.
 */
public record CrearTramiteRequest(
        String rutContribuyente,
        String tipo) {
}
