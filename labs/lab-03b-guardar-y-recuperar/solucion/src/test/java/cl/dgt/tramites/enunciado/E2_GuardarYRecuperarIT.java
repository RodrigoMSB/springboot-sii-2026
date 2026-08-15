package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.domain.entity.ObservacionInterna;
import cl.dgt.tramites.infrastructure.repository.ContribuyenteRepository;
import cl.dgt.tramites.infrastructure.repository.ObservacionInternaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_2 · Un objeto entra a la base y vuelve.
 *
 * <p>El repositorio se busca en el contexto y no se inyecta directo: mientras el TODO_2 no esté
 * hecho, la interfaz no es un repositorio de Spring Data y no existe bean alguno. Buscarlo así
 * permite fallar con un mensaje que dice qué falta, en vez de con un error de arranque.
 */
@DisplayName("TODO_2 · el repositorio guarda un objeto y lo recupera")
class E2_GuardarYRecuperarIT extends BaseObservacionesIT {

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private ContribuyenteRepository contribuyentes;

    private ObservacionInternaRepository repositorio() {
        ObservacionInternaRepository repo = contexto
                .getBeanProvider(ObservacionInternaRepository.class).getIfAvailable();
        assertThat(repo)
                .as("no hay ningún bean de ObservacionInternaRepository: la interfaz todavía no "
                    + "extiende JpaRepository (TODO_2)")
                .isNotNull();
        return repo;
    }

    @Test
    @Transactional
    @DisplayName("save() convierte el objeto en fila y le devuelve el id que generó el motor")
    void guardarDevuelveElIdGenerado() {
        var contribuyente = contribuyentes.findByRut(RUT).orElseThrow();

        ObservacionInterna guardada = repositorio()
                .save(new ObservacionInterna(contribuyente, "Primera observación.", AUTOR));

        assertThat(guardada.getId())
                .as("el id lo pone el motor (BIGSERIAL) y Hibernate lo escribe de vuelta en el "
                    + "objeto: si viene null, la fila no llegó a la base")
                .isNotNull();
    }

    /**
     * {@code @Transactional} hace falta porque la relación con el contribuyente es LAZY: leer su
     * RUT fuera de una sesión de persistencia lanza {@code LazyInitializationException}. Es la
     * misma razón por la que el servicio lo necesita, y verlo aquí primero ayuda.
     */
    @Test
    @Transactional
    @DisplayName("La búsqueda por RUT recupera lo guardado, y no lo del vecino")
    void recuperaLoGuardadoYNadaMas() {
        var valentina = contribuyentes.findByRut(RUT).orElseThrow();
        var vecino = contribuyentes.findByRut(RUT_VECINO).orElseThrow();
        var repo = repositorio();

        repo.save(new ObservacionInterna(valentina, "Presenta dentro de plazo.", AUTOR));
        repo.save(new ObservacionInterna(valentina, "Solicitó un certificado.", AUTOR));
        repo.save(new ObservacionInterna(vecino, "Esto es de otro contribuyente.", AUTOR));

        var deValentina = repo.findByContribuyenteRut(RUT);
        var delVecino = repo.findByContribuyenteRut(RUT_VECINO);

        assertThat(deValentina).extracting(ObservacionInterna::getTexto)
                .as("tiene que devolver las dos que se guardaron para este contribuyente")
                .contains("Presenta dentro de plazo.", "Solicitó un certificado.");
        assertThat(deValentina)
                .as("y SOLO las suyas: la consulta filtra por el RUT que se le pasa")
                .allSatisfy(o -> assertThat(o.getContribuyente().getRut()).isEqualTo(RUT));
        assertThat(deValentina).extracting(ObservacionInterna::getTexto)
                .as("y lo del vecino NO aparece aquí: eso es filtrar de verdad")
                .doesNotContain("Esto es de otro contribuyente.");
        assertThat(delVecino).extracting(ObservacionInterna::getTexto)
                .as("lo del vecino queda del lado del vecino")
                .contains("Esto es de otro contribuyente.");
    }
}
