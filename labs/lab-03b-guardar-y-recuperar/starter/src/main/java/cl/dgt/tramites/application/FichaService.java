package cl.dgt.tramites.application;

import cl.dgt.tramites.web.dto.FichaContribuyenteDTO;

/**
 * El caso de uso "consultar la ficha de un contribuyente".
 *
 * <p>Es una <strong>interfaz</strong>, y eso no es ceremonia. El controlador depende de este
 * contrato, no de una implementación: puedes probar el controlador con un doble de test sin
 * tocar la base de datos, y puedes cambiar cómo se arma la ficha sin recompilar la capa web.
 */
public interface FichaService {

    /**
     * @throws cl.dgt.tramites.domain.exception.ContribuyenteNoEncontradoException si el RUT
     *         no corresponde a ningún contribuyente.
     */
    FichaContribuyenteDTO fichaDe(String rut);
}
