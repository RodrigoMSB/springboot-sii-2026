package cl.dgt.tramites.web.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida un RUT chileno: cuerpo numérico + dígito verificador por módulo 11.
 *
 * <p><strong>TODO_2 — impleméntalo (≈15 min).</strong> Los tests {@code E2_RutValidoTest} te
 * dan seis casos: RUTs válidos, RUTs con dígito verificador falso, y basura de formato. No
 * se pasan hardcodeando: un {@code return true} pasa los válidos y falla los inválidos; un
 * {@code return false}, al revés. La red triangula.
 *
 * <p><em>El módulo 11:</em> multiplica cada dígito del cuerpo (de derecha a izquierda) por la
 * serie cíclica 2,3,4,5,6,7; suma; {@code 11 - (suma % 11)} da el DV (resultado 10 → 'K',
 * 11 → '0'). Acepta el RUT con o sin puntos, con guion, DV en dígito o 'K'.
 *
 * <p>Pista: normaliza primero (quita puntos y guion, a mayúsculas), separa cuerpo y DV,
 * valida que el cuerpo sea numérico, y compara el DV calculado con el ingresado.
 */
public class RutValidator implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }
}
