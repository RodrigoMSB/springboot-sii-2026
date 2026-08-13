package cl.dgt.tramites.integracion;

import org.junit.jupiter.api.Test;

/**
 * TODO_2 · Escribe TÚ la prueba de integración de punta a punta (≈15 min).
 *
 * <p>Tuya, fuera de {@code enunciado/}. El {@code 90} comprueba que existe y pasa. Es tu primera
 * IT completa: {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} + la base embebida vía
 * {@code @DynamicPropertySource} sobre {@code PostgresEmbebido} + {@code RestTestClient}
 * pegándole por HTTP.
 *
 * <p>Prueba que el listado pagina de verdad: pide dos páginas distintas y verifica que el
 * contenido cambia. (Mira `E2_ListadoFuncionalIT` para el patrón de RestTestClient.)
 */
class ListadoIntegracionTest {

    @Test
    void escribeTuIntegracion() {
        throw new UnsupportedOperationException("{{TODO_2}}");
    }
}
