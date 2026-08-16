package cl.dgt.tramites.enunciado;

import cl.dgt.tramites.PostgresEmbebido;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base de los tests de resiliencia: PostgreSQL real (embebido) + TESO (WireMock, in-process),
 * y los helpers para gobernar a TESO (ponerle un retraso, resetearlo). TESO arranca sin
 * mappings: cada test declara la respuesta que necesita. Cero {@code Thread.sleep}.
 *
 * <p><strong>TESO ya no es un contenedor: es una librería</strong> (SPEC-025). WireMock nació
 * librería; la imagen Docker es un envoltorio sobre ella. Al quitar el envoltorio se gana lo
 * obvio —el lab corre en una máquina sin Docker, que es la del alumno del SII— y algo menos
 * obvio: el impostor se gobierna por <em>API Java directa</em> en vez de por HTTP contra su
 * puerto de administración. Menos piezas, menos latencia, y errores en tiempo de compilación
 * en lugar de un JSON mal formado que solo se descubre corriendo.
 *
 * <p><strong>Un servidor por JVM, en puerto dinámico.</strong> Arranca en el bloque estático
 * por el mismo motivo que el motor de PostgreSQL: Spring cachea el contexto y lo reutiliza
 * entre clases de test, así que un TESO con ciclo de vida por-clase se apagaría dejando al
 * contexto cacheado apuntando a un puerto muerto. El puerto lo elige el sistema (0) y se
 * publica por {@code @DynamicPropertySource}: dos suites en paralelo no se pisan.
 *
 * <p><strong>Atado a 127.0.0.1 a propósito.</strong> Por defecto WireMock escucha en todas las
 * interfaces, y eso en Windows dispara el cartel del Firewall pidiendo permiso de
 * administrador — el que el alumno de una máquina corporativa no tiene (SPEC-024 · A2.3).
 * El impostor solo tiene que ser alcanzable desde esta misma máquina.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "dgt.base-embebida.enabled=false")
abstract class BaseResilienciaIT {

    static final String CLAVE = "dgt-2026";
    static final String CAROLINA = "9876543-2";   // FUNCIONARIO: crea, paga, emite

    static final WireMockServer TESO =
            new WireMockServer(options().port(0).bindAddress("127.0.0.1"));

    static {
        TESO.start();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        // Una base recién creada para ESTE contexto: se pide una sola vez y se guarda.
        String url = PostgresEmbebido.nuevaBase();
        registro.add("spring.datasource.url", () -> url);
        registro.add("spring.datasource.username", PostgresEmbebido::usuario);
        registro.add("spring.datasource.password", PostgresEmbebido::clave);
        registro.add("dgt.teso.base-url", BaseResilienciaIT::tesoUrl);
    }

    static String tesoUrl() {
        return "http://127.0.0.1:" + TESO.port();
    }

    @LocalServerPort
    int puerto;

    @BeforeEach
    void limpiarTeso() {
        TESO.resetAll();
    }

    /**
     * El cliente de los tests, con el transporte FIJADO al del JDK a propósito.
     *
     * <p><strong>Sin este {@code JdkClientHttpRequestFactory} el laboratorio se miente a sí
     * mismo</strong>, y costó medirlo. Spring elige el transporte de {@code RestTestClient} por
     * lo que encuentre en el classpath cuando nadie se lo dice, y prefiere Apache HttpClient 5
     * sobre el del JDK. WireMock arrastra Apache HttpClient 5 —lo usa por dentro, no se puede
     * excluir— así que al traer TESO como librería, este cliente cambió de transporte sin que
     * nadie lo pidiera.
     *
     * <p>Y Apache HttpClient 5 <strong>reintenta ante un 503</strong>, esperando un segundo. El
     * 503 es justo lo que este lab enseña a devolver cuando TESO no contesta: el cliente de test
     * se tragaba la degradación elegante, esperaba y repetía el pago entero. Medido:
     *
     * <pre>
     *   transporte autodetectado (Apache):  2 llamadas a TESO · 2660 ms · E1_TimeoutIT FALLA
     *   transporte fijado (JDK):            1 llamada  a TESO ·  864 ms · E1_TimeoutIT pasa
     * </pre>
     *
     * <p>La lección, que vale más que el arreglo: <strong>un test que mide tiempos o cuenta
     * llamadas no puede dejar su transporte a la autodetección por classpath</strong>. Una
     * dependencia nueva, en otra capa, se lo cambia sin avisar.
     */
    RestTestClient cliente() {
        return RestTestClient.bindToServer(new JdkClientHttpRequestFactory())
                .baseUrl("http://localhost:" + puerto).build();
    }

    /**
     * Configura TESO para que confirme el pago con un retraso de {@code ms} milisegundos.
     *
     * <p>Antes esto era un POST de JSON contra {@code /__admin/mappings}. Ahora es la misma
     * declaración escrita en Java: si te equivocas en el nombre de un método, no compila.
     */
    void tesoRespondeCon(int ms) {
        TESO.stubFor(get(urlPathMatching("/pagos/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"confirmado\":true}")
                        .withFixedDelay(ms)));
    }

    @SuppressWarnings("unchecked")
    String bearer(String rut) {
        Map<String, Object> cuerpo = cliente().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rut", rut, "clave", CLAVE))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody();
        return "Bearer " + cuerpo.get("token");
    }

    /** Crea un trámite y lo avanza a PRESENTADO (listo para pagarse). Devuelve su id. */
    @SuppressWarnings("unchecked")
    Long crearTramitePresentado(String bearer) {
        Map<String, Object> creado = cliente().post().uri("/api/v1/tramites")
                .header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("rutContribuyente", "11111111-1", "tipo", "DECLARACION_F29"))
                .exchange().expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        Long id = ((Number) creado.get("id")).longValue();
        cliente().post().uri("/api/v1/tramites/" + id + "/avanzar?a=PRESENTADO")
                .header("Authorization", bearer)
                .exchange().expectStatus().isOk();
        return id;
    }
}
