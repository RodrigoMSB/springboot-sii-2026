package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** TODO_3 · JPQL multi-entidad. Y la prohibición del JOIN FETCH, verificada como test. */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BasePersistenciaIT.class)
class E3_JpqlMultiEntidadIT {

    @Autowired TramiteRepository tramites;

    @Test
    @DisplayName("presentadosDelPeriodo trae los trámites no-borrador de un período")
    void presentadosDelPeriodo() {
        // La semilla de 2026-05 tiene dos F29 (uno PRESENTADO, uno PAGADO); ninguno BORRADOR.
        assertThat(tramites.presentadosDelPeriodo("2026-05"))
                .as("dos trámites de 2026-05 no están en BORRADOR")
                .hasSize(2);
    }

    @Test
    @DisplayName("la consulta NO usa JOIN FETCH: eso es del Lab 05, no de hoy")
    void laConsultaNoUsaJoinFetch() throws NoSuchMethodException {
        // Se lee la anotación @Query por reflexión: es un TEST, no un grep del validador.
        Method metodo = TramiteRepository.class.getMethod("presentadosDelPeriodo", String.class);
        Query query = metodo.getAnnotation(Query.class);
        assertThat(query).as("presentadosDelPeriodo debe llevar @Query").isNotNull();
        assertThat(query.value().toLowerCase())
                .as("JOIN FETCH existe, y es la respuesta a una pregunta que aún no te hiciste")
                .doesNotContain("join fetch");
    }
}
