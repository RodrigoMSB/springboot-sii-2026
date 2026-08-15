package cl.dgt.tramites.enunciado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · El nombre del método ES la consulta.
 *
 * <p>El repositorio se busca en el contexto y no se inyecta directo: mientras el TODO_2 no esté
 * hecho, la interfaz no es un repositorio de Spring Data y no existe bean alguno. Buscarlo así
 * permite fallar con un mensaje que dice qué falta, en vez de con un error de arranque.
 */
@DisplayName("TODO_2 · findByContribuyenteRut, y el apóstrofe deja de ser código")
class E2_ConsultaDerivadaIT extends BaseObservacionesIT {

    @Autowired
    private ApplicationContext contexto;

    private Object repositorio() {
        Object repo = contexto.getBeanProvider(
                cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository.class)
                .getIfAvailable();
        assertThat(repo)
                .as("no hay ningún bean de ObservacionInternaRepository: la interfaz todavía no "
                    + "extiende JpaRepository (TODO_2)")
                .isNotNull();
        return repo;
    }

    /**
     * {@code @Transactional} no está por capricho: la relación con el contribuyente es LAZY, y
     * leer su RUT fuera de una sesión de persistencia lanza {@code LazyInitializationException}.
     * Es la misma razón por la que el servicio lo necesita, y verlo aquí primero ayuda.
     */
    @Test
    @Transactional(readOnly = true)
    @DisplayName("Devuelve las observaciones del contribuyente que se pide, y solo esas")
    void devuelveLasDelContribuyente() {
        var repo = (cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository) repositorio();

        var observaciones = repo.findByContribuyenteRut(RUT_INOCENTE);

        assertThat(observaciones)
                .as("Valentina tiene %s observaciones en la semilla", OBSERVACIONES_DE_VALENTINA)
                .hasSize(OBSERVACIONES_DE_VALENTINA);
        assertThat(observaciones)
                .allSatisfy(o -> assertThat(o.getContribuyente().getRut()).isEqualTo(RUT_INOCENTE));
    }

    @Test
    @DisplayName("el_apostrofe_ya_no_es_codigo")
    void el_apostrofe_ya_no_es_codigo() {
        var repo = (cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository) repositorio();

        var observaciones = repo.findByContribuyenteRut(RUT_MALICIOSO);

        assertThat(observaciones)
                .as("el RUT viaja como PARÁMETRO, no pegado a la consulta: el motor busca a "
                    + "alguien cuyo RUT sea literalmente «%s», no lo encuentra, y devuelve vacío. "
                    + "Si aquí vinieran %s filas, el apóstrofe seguiría siendo código.",
                    RUT_MALICIOSO, OBSERVACIONES_TOTALES)
                .isEmpty();
    }
}
