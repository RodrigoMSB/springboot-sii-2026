package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base de los tests del Lab 3.5: PostgreSQL de verdad, embebido, uno por contexto.
 *
 * <p>El RUT inocente y el malicioso viven aquí porque los usan varios tests, y porque conviene
 * verlos juntos: la única diferencia entre ellos es un apóstrofe.
 *
 * <p>La semilla (V3) deja DOS contribuyentes con observaciones. Valentina tiene 2; «Comercial
 * Andina SpA» tiene 3, y esas 3 son las que jamás deberían aparecer cuando alguien pregunta
 * por Valentina.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
abstract class BaseObservacionesIT {

    /** Valentina Rojas. Tiene 2 observaciones en la semilla. */
    static final String RUT_INOCENTE = "11111111-1";

    /** El mismo RUT, más un apóstrofe y una condición siempre verdadera. */
    static final String RUT_MALICIOSO = "11111111-1' OR '1'='1";

    /** Observaciones de Valentina en la semilla (V3). */
    static final int OBSERVACIONES_DE_VALENTINA = 2;

    /** Todas las observaciones de la tabla, de todos los contribuyentes. */
    static final int OBSERVACIONES_TOTALES = 5;

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
    }
}
