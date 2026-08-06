package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.domain.entity.Tramite;
import cl.dgt.tramites.infrastructure.repository.TramiteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO_1 · Pedir un trámite NO arrastra su árbol. Las relaciones son LAZY.
 *
 * <p>No se cuenta consultas (esa herramienta es del Lab 05, y no se adelanta). Se le pregunta
 * a JPA, con {@link PersistenceUnitUtil#isLoaded}, si la relación YA está cargada. Para una
 * relación LAZY recién traída y aún no accedida: {@code false}. Para una EAGER: {@code true}
 * — porque viajó junto con el trámite, la pidieras o no.
 */
@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(BasePersistenciaIT.class)
@Transactional
class E1_RelacionesLazyIT {

    @Autowired TramiteRepository tramites;
    @Autowired EntityManager em;

    @Test
    @DisplayName("findById de un trámite no carga su contribuyente ni sus adjuntos (son LAZY)")
    void findByIdNoCargaLasRelaciones() {
        Tramite t = tramites.findAll().getFirst();
        PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();

        // @ManyToOne: LAZY entrega un proxy no inicializado. Con EAGER (el crimen), viaja
        // cargado y esto sería true.
        assertThat(util.isLoaded(t, "contribuyente"))
                .as("el contribuyente NO debe venir cargado: es una relación LAZY")
                .isFalse();
        // @OneToMany: la colección LAZY no se toca hasta iterarla.
        assertThat(util.isLoaded(t, "adjuntos"))
                .as("los adjuntos NO deben venir cargados: es una colección LAZY")
                .isFalse();
    }
}
