package cl.dgt.tramites.application;

import cl.dgt.tramites.config.DgtProperties;
import org.springframework.stereotype.Service;

/**
 * El sello que la DGT estampa en todo documento que emite.
 *
 * <p>Consume {@link DgtProperties}: configuración <em>tipada</em>. El compilador sabe que
 * {@code largo} es un {@code int}, y la aplicación no arranca si alguien escribe
 * {@code dgt.folio.largo: ocho}.
 */
@Service
public class SelloService {

    private final DgtProperties propiedades;

    public SelloService(DgtProperties propiedades) {
        this.propiedades = propiedades;
    }

    /** Ejemplo: {@code Dirección General de Tributación · DGT-00000042}. */
    public String sellar(long numero) {
        String relleno = String.format("%0" + propiedades.folio().largo() + "d", numero);
        return propiedades.institucion() + " · " + propiedades.folio().prefijo() + "-" + relleno;
    }

    public String institucion() {
        return propiedades.institucion();
    }
}
