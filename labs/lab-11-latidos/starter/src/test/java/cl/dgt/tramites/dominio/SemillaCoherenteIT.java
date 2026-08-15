package cl.dgt.tramites.dominio;

import cl.dgt.tramites.PostgresEmbebido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El guardián de la semilla.
 *
 * <p>La bitácora declaró que «`contador_folio` nace en 1, coherente con el trámite FOLIADO
 * que porta el folio 1» y que esa coherencia es invariante de toda semilla futura. Era una
 * frase. Aquí muerde.
 *
 * <p>Por qué importa: si el contador quedara por debajo del último folio emitido, el Lab 06
 * emitiría un folio <em>repetido</em> en su primer intento, y la lección sobre concurrencia
 * quedaría contaminada por un bug de semilla. El alumno depuraría el enunciado en vez del
 * problema.
 *
 * <p>Verifica contra la base real, después de Flyway. Nadie declara "sin duplicados" sin un
 * mecanismo que lo mida: esa es la anti-herencia A-02 del ADN.
 */
@SpringBootTest(properties = "dgt.base-embebida.enabled=false")
class SemillaCoherenteIT {

    @DynamicPropertySource
    static void baseDeDatos(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("El contador de folios va exactamente al último folio emitido")
    void elContadorNoSeQuedaAtras() {
        Long contador = jdbc.queryForObject(
                "SELECT ultimo_numero FROM contador_folio WHERE id = 1", Long.class);
        Long maximo = jdbc.queryForObject(
                "SELECT COALESCE(MAX(numero), 0) FROM folio", Long.class);

        assertThat(contador)
                .as("Si el contador < MAX(folio.numero), el Lab 06 emitiría un folio repetido "
                    + "en su primer intento y culparía a su propio código.")
                .isEqualTo(maximo);
    }

    @Test
    @DisplayName("Todo trámite FOLIADO tiene folio")
    void todoFoliadoTieneSuFolio() {
        Integer foliadosSinFolio = jdbc.queryForObject("""
                SELECT COUNT(*) FROM tramite t
                 WHERE t.estado = 'FOLIADO'
                   AND NOT EXISTS (SELECT 1 FROM folio f WHERE f.tramite_id = t.id)
                """, Integer.class);

        assertThat(foliadosSinFolio)
                .as("un trámite FOLIADO sin folio es un estado imposible")
                .isZero();
    }

    @Test
    @DisplayName("Ningún trámite no-FOLIADO tiene folio")
    void nadieMasTieneFolio() {
        Integer noFoliadosConFolio = jdbc.queryForObject("""
                SELECT COUNT(*) FROM folio f
                  JOIN tramite t ON t.id = f.tramite_id
                 WHERE t.estado <> 'FOLIADO'
                """, Integer.class);

        assertThat(noFoliadosConFolio)
                .as("un folio emitido implica el estado FOLIADO: no se folia y luego se retrocede")
                .isZero();
    }

    @Test
    @DisplayName("La semilla dejó exactamente un folio, y es el número 1")
    void laSemillaEsLaQueCreemos() {
        Integer cuantos = jdbc.queryForObject("SELECT COUNT(*) FROM folio", Integer.class);
        Long primero = jdbc.queryForObject("SELECT MIN(numero) FROM folio", Long.class);

        assertThat(cuantos).isEqualTo(1);
        assertThat(primero).isEqualTo(1L);
    }
}
