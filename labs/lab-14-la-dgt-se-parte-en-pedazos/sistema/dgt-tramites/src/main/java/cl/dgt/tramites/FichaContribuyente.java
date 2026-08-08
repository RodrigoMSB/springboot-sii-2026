package cl.dgt.tramites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * La misma ficha que publica {@code dgt-contribuyentes}, declarada aquí otra vez.
 *
 * <p>Sí: es duplicación, y es deliberada. La alternativa —un módulo Maven
 * compartido con los DTOs— parece más limpia y es una de las trampas más caras
 * de esta arquitectura: el día que quieras cambiar un campo, tienes que
 * desplegar los dos servicios a la vez, y acabas de perder la independencia por
 * la que partiste el sistema. Se llama <em>acoplamiento por artefacto
 * compartido</em>.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} es la otra mitad de la
 * decisión: si el otro servicio añade un campo mañana, este no se rompe. Un
 * contrato tolerante es lo que permite desplegar los dos por separado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FichaContribuyente(String rut, String razonSocial, String atendidoPor) {
}
