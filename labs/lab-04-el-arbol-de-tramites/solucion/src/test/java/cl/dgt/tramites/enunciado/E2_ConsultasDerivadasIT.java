package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.domain.tipo.EstadoTramite;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

/** TODO_2 · Consultas derivadas: el nombre del método es la consulta. Contra la semilla. */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(BasePersistenciaIT.class)
class E2_ConsultasDerivadasIT {

    @Autowired TramiteRepository tramites;

    @Test
    @DisplayName("findByContribuyenteRut trae los trámites de Comercial Andina")
    void findByRut() {
        assertThat(tramites.findByContribuyenteRut("12345678-5"))
                .as("Comercial Andina (12345678-5) tiene 3 trámites en la semilla")
                .hasSize(3);
    }

    @Test
    @DisplayName("findByContribuyenteRut con Pageable respeta el tamaño de página")
    void findByRutPaginado() {
        assertThat(tramites.findByContribuyenteRut("12345678-5", PageRequest.of(0, 2)).getContent())
                .hasSize(2);
    }

    @Test
    @DisplayName("existsByContribuyenteRutAndEstado detecta un trámite FOLIADO")
    void existsPorEstado() {
        assertThat(tramites.existsByContribuyenteRutAndEstado("12345678-5", EstadoTramite.FOLIADO))
                .isTrue();
        assertThat(tramites.existsByContribuyenteRutAndEstado("11111111-1", EstadoTramite.FOLIADO))
                .isFalse();
    }

    @Test
    @DisplayName("countByEstado cuenta los trámites en BORRADOR")
    void countPorEstado() {
        assertThat(tramites.countByEstado(EstadoTramite.BORRADOR))
                .as("la semilla tiene 1 trámite en BORRADOR")
                .isEqualTo(1);
    }
}
