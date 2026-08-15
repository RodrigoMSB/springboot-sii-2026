package cl.dgt.tramites.web.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida un RUT chileno: cuerpo numérico + dígito verificador por módulo 11.
 *
 * <p>Formato aceptado: con o sin puntos, con guion, DV en dígito o 'K'. Ejemplos válidos:
 * {@code 11.111.111-1}, {@code 12345678-5}, {@code 9876543-2}.
 *
 * <p>El módulo 11: se multiplica cada dígito del cuerpo (de derecha a izquierda) por la serie
 * 2,3,4,5,6,7 cíclica; se suma; {@code 11 - (suma % 11)} da el DV (10 → 'K', 11 → '0').
 */
public class RutValidator implements ConstraintValidator<RutValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        String limpio = valor.replace(".", "").replace("-", "").trim().toUpperCase();
        if (limpio.length() < 2) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        if (!cuerpo.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return dvEsperado(cuerpo) == dvIngresado;
    }

    private char dvEsperado(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) return '0';
        if (resto == 10) return 'K';
        return (char) ('0' + resto);
    }
}
