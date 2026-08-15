package cl.dgt.tramites.web.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * El campo debe ser un RUT chileno válido: cuerpo + dígito verificador por módulo 11.
 *
 * <p>Es una anotación PROPIA. Bean Validation trae {@code @NotBlank} y {@code @Pattern},
 * pero ninguna sabe qué es un RUT: eso es regla de negocio chilena, y vive en su validador.
 * El mensaje se resuelve desde {@code ValidationMessages.properties} (i18n).
 */
@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface RutValido {
    String message() default "{cl.dgt.rut.invalido}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
