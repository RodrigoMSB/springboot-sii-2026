package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base de los tests del Lab 3.5: PostgreSQL de verdad, embebido, uno por contexto.
 *
 * <p>La tabla {@code observacion_interna} nace VACÍA. Cada test guarda lo que necesita y lo
 * recupera: eso es exactamente lo que el laboratorio enseña a hacer.
 *
 * <p>Los dos RUT salen de la semilla del tronco (V2) y existen desde el Lab 01: Valentina y
 * «Comercial Andina SpA». Tener dos permite comprobar que la búsqueda trae las observaciones
 * del contribuyente que se pide, y no las del vecino.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
abstract class BaseObservacionesIT {

    /** Valentina Rojas. */
    static final String RUT = "11111111-1";

    /** Comercial Andina SpA — el vecino, para comprobar que no se mezclan. */
    static final String RUT_VECINO = "12345678-5";

    static final String AUTOR = "Carolina Espinoza";

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
    }
}
