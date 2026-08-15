package cl.dgt.tramites.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * La configuración de la DGT, tipada y validada.
 *
 * <p>Un {@code record} inmutable: nadie puede cambiar el prefijo del folio en caliente.
 * Si falta {@code dgt.institucion} o {@code dgt.folio.largo} es 3, la aplicación
 * <strong>no arranca</strong> y te dice exactamente qué propiedad está mal. Compáralo con
 * un {@code @Value}: ahí el error llega en producción, cuando alguien concatena un
 * {@code null} en un folio.
 *
 * <p>La validación no es adorno: {@code @Min(6)} sobre el largo impide emitir folios de
 * tres dígitos, que es una decisión de negocio disfrazada de configuración.
 */
@Validated
@ConfigurationProperties(prefix = "dgt")
public record DgtProperties(

        @NotBlank(message = "dgt.institucion no puede estar vacío")
        String institucion,

        @Valid
        Folio folio) {

    public record Folio(
            @NotBlank(message = "dgt.folio.prefijo no puede estar vacío")
            String prefijo,

            @Min(value = 6, message = "dgt.folio.largo debe ser al menos 6")
            int largo) {
    }
}
