package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.application.ListadoService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El contador. Este test NO mira el resultado del listado: mira su COSTO.
 *
 * <p>Dos listados que devuelven exactamente lo mismo pueden costar 3 consultas o 300. El ojo
 * no distingue; este test sí. Es el arquetipo del criterio de aceptación que no se aprueba
 * tecleando más código: se aprueba haciendo la consulta correcta.
 *
 * <p>Presupuesto: una página del listado debe costar {@value #PRESUPUESTO} consultas o menos.
 * El número no es mágico: es "los datos, y el conteo de la paginación, y nada más". Un listado
 * que itera relaciones (el N+1) lo revienta; una proyección lo cumple.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
@Import(BaseRendimientoIT.class)
class E1_ContadorDeConsultasIT {

    /** Presupuesto de consultas para una página. Datos + count de paginación. */
    static final int PRESUPUESTO = 3;

    @Autowired ListadoService listado;
    @Autowired EntityManagerFactory emf;

    @Test
    @DisplayName("una página del listado cuesta a lo más 3 consultas, sin importar cuántos trámites haya")
    void elListadoPaginadoNoDisparaNMasUno() {
        // Se pide una página; el costo NO debe crecer con el nº de filas de la tabla.
        long consultas = BaseRendimientoIT.consultasDe(emf,
                () -> listado.listar(PageRequest.of(0, 20)));

        assertThat(consultas)
                .as("el N+1 dispara una consulta por trámite; una proyección, no")
                .isLessThanOrEqualTo(PRESUPUESTO);
    }
}
