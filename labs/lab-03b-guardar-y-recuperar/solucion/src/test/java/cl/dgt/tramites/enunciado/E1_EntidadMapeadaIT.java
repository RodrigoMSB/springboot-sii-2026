package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.domain.entity.ObservacionInterna;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * TODO_1 · La clase ES la tabla.
 *
 * <p>No se prueba «que compile»: se le pregunta a Hibernate, en su propio metamodelo, si conoce
 * a {@code ObservacionInterna} y si la relación con el contribuyente está declarada como toca.
 */
@DisplayName("TODO_1 · ObservacionInterna está mapeada a su tabla")
class E1_EntidadMapeadaIT extends BaseObservacionesIT {

    @Autowired
    private EntityManagerFactory emf;

    @Test
    @DisplayName("Hibernate conoce la entidad y la asocia a la tabla observacion_interna")
    void laEntidadEstaMapeada() {
        Throwable buscando = catchThrowable(() -> emf.getMetamodel().entity(ObservacionInterna.class));

        assertThat(buscando)
                .as("Hibernate no conoce esta clase: todavía no es una @Entity (TODO_1)")
                .isNull();

        EntityType<ObservacionInterna> tipo = emf.getMetamodel().entity(ObservacionInterna.class);
        assertThat(tipo.getAttributes())
                .as("la entidad debe mapear sus campos")
                .extracting("name")
                .contains("id", "contribuyente", "texto", "autor", "creadaEn");
    }

    @Test
    @DisplayName("El @ManyToOne al contribuyente es LAZY, no EAGER")
    void laRelacionEsLazy() throws Exception {
        var relacion = ObservacionInterna.class.getDeclaredField("contribuyente");
        var anotacion = relacion.getAnnotation(jakarta.persistence.ManyToOne.class);

        assertThat(anotacion)
                .as("falta el @ManyToOne hacia Contribuyente (TODO_1)")
                .isNotNull();
        assertThat(anotacion.fetch())
                .as("EAGER es el valor por defecto, y es el que el Lab 04 te va a hacer pagar: "
                    + "pedir una observación traería al contribuyente y con él sus trámites")
                .isEqualTo(jakarta.persistence.FetchType.LAZY);
    }
}
