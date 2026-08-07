package cl.dgt.tramites.application;

/**
 * El único punto por donde la DGT habla con Tesorería (P-06: la escalera colapsada). Las formas
 * acumuladas de llamar a TESO —RestClient a mano, un cliente declarativo— terminan detrás de esta
 * interfaz. La aplicación depende del PUERTO, no del transporte: si mañana TESO cambia de protocolo,
 * cambia el adaptador, no el servicio.
 */
public interface TesoreriaPort {

    /**
     * Confirma el pago de un trámite contra TESO.
     *
     * @throws cl.dgt.tramites.domain.exception.TesoreriaNoDisponibleException si TESO no responde
     *         a tiempo o falla. La app NO se queda esperando: falla rápido y con dignidad.
     */
    ConfirmacionPago confirmarPago(String referencia);
}
