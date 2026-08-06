package cl.dgt.tramites.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * El sello que la DGT estampa en todo documento que emite.
 *
 * <p><strong>Míralo bien: este código huele.</strong> Tres {@code @Value} sueltos, tres
 * cadenas mágicas, cero validación. El compilador no sabe que {@code largo} debería ser un
 * número razonable; Spring tampoco. Si mañana alguien borra {@code dgt.folio.prefijo} del
 * YAML, esta clase estampa {@code null-00000042} en un documento oficial.
 *
 * <p>El TODO_3 te pide reemplazar esto por {@code DgtProperties}. Cuando lo hagas, borra
 * los tres {@code @Value} y recibe el record por constructor.
 */
@Service
public class SelloService {

    private final String institucion;
    private final String prefijo;
    private final int largo;

    public SelloService(
            @Value("${dgt.institucion}") String institucion,
            @Value("${dgt.folio.prefijo}") String prefijo,
            @Value("${dgt.folio.largo}") int largo) {
        this.institucion = institucion;
        this.prefijo = prefijo;
        this.largo = largo;
    }

    /** Ejemplo: {@code Dirección General de Tributación · DGT-00000042}. */
    public String sellar(long numero) {
        return institucion + " · " + prefijo + "-" + String.format("%0" + largo + "d", numero);
    }

    public String institucion() {
        return institucion;
    }
}
