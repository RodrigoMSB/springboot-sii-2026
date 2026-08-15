package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.ObservacionInternaService;
import cl.dgt.tramites.application.ObservacionInternaVista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_3 · El servicio conectado, de punta a punta.
 *
 * <p>Aquí no se toca el repositorio: se le pide al servicio que guarde y se le pide que
 * recupere, que es lo que hace la aplicación de verdad. Si el TODO_3 está bien, un objeto
 * entra por un método y vuelve por el otro sin que nadie haya escrito una línea de SQL.
 */
@DisplayName("TODO_3 · el servicio guarda y recupera usando el repositorio")
class E3_ServicioConectadoIT extends BaseObservacionesIT {

    @Autowired
    private ApplicationContext contexto;

    private ObservacionInternaService servicio() {
        ObservacionInternaService s = contexto
                .getBeanProvider(ObservacionInternaService.class).getIfAvailable();
        assertThat(s)
                .as("no hay bean de ObservacionInternaService en el contexto")
                .isNotNull();
        return s;
    }

    @Test
    @DisplayName("Lo que el servicio guarda, el servicio lo recupera")
    void guardaYRecupera() {
        var servicio = servicio();

        ObservacionInternaVista guardada =
                servicio.guardar(RUT, "Revisión anual sin hallazgos.", AUTOR);

        assertThat(guardada.rutContribuyente()).isEqualTo(RUT);
        assertThat(guardada.creadaEn())
                .as("la fecha la pone la entidad al construirse: si viene null, no se guardó")
                .isNotNull();

        assertThat(servicio.porRut(RUT))
                .as("recuperar tiene que devolver lo que se acaba de guardar")
                .extracting(ObservacionInternaVista::texto)
                .contains("Revisión anual sin hallazgos.");
    }

    @Test
    @DisplayName("Guardar dos veces acumula: cada save es una fila nueva")
    void guardarDosVecesAcumula() {
        var servicio = servicio();

        servicio.guardar(RUT_VECINO, "Primera nota del vecino.", AUTOR);
        servicio.guardar(RUT_VECINO, "Segunda nota del vecino.", AUTOR);

        assertThat(servicio.porRut(RUT_VECINO))
                .as("cada objeto nuevo que pasa por save() es un INSERT: dos llamadas, dos filas")
                .extracting(ObservacionInternaVista::texto)
                .contains("Primera nota del vecino.", "Segunda nota del vecino.");
    }
}
